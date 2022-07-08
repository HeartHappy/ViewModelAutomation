package com.hearthappy.processor

import com.hearthappy.annotations.BindLiveData
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.processor.model.GenerateViewModelData
import com.hearthappy.processor.model.RequestData
import com.hearthappy.processor.tools.asKotlinPackage
import com.hearthappy.processor.tools.splitPackage
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.KClass

internal fun ViewModelProcessor.generatePropertyAndMethodByStateFlow(
    classBuilder: TypeSpec.Builder,
    requestDataList: List<RequestData>,
    bindStateFlow: Array<BindStateFlow>?,
    finishBlock: (BindStateFlow, List<RequestData>, GenerateViewModelData) -> Unit
) {
    bindStateFlow?.apply {
        val mutableStateFlow =
            ClassName(STATE_FLOW_PKG, MUTABLE_STATE_FLOW)
        val stateFlow = ClassName(STATE_FLOW_PKG, STATE_FLOW)
        val requestState =
            ClassName(KTOR_NETWORK_PKG, KTOR_REQUEST_STATE)
        forEach {
            val viewModelParam = it.getViewModelParam()

            sendNoteMsg("==================> Create a private StateFlow")
            generatePrivateProperty(
                propertyName = viewModelParam.priPropertyName,
                propertyType = mutableStateFlow.parameterizedBy(
                    requestState.parameterizedBy(viewModelParam.responseBody)
                ),
                delegateValue = "${MUTABLE_STATE_FLOW}(${KTOR_REQUEST_STATE}.DEFAULT)",
                addToClass = classBuilder,
                KModifier.PRIVATE
            )

            sendNoteMsg("==================> Create a public StateFlow") //创建公开属性
            generatePublicProperty(
                propertyName = viewModelParam.pubPropertyName,
                propertyType = stateFlow.parameterizedBy(
                    requestState.parameterizedBy(viewModelParam.responseBody)
                ),
                initValue = viewModelParam.priPropertyName,
                addToClass = classBuilder
            )

            finishBlock(it,requestDataList,viewModelParam)
        }
    }
}


internal fun ViewModelProcessor.generatePropertyAndMethodByLiveData(
    classBuilder: TypeSpec.Builder,
    requestDataList: List<RequestData>,
    bindLiveData: Array<BindLiveData>?,
    finishBlock: (BindLiveData, List<RequestData>, GenerateViewModelData) -> Unit
) {
    bindLiveData?.apply {
        val mutableLiveData =
            ClassName(LIVEDATA_PKG, MUTABLE_LIVEDATA)
        val liveData = ClassName(LIVEDATA_PKG, LIVEDATA)
        val result =
            ClassName(KTOR_NETWORK_PKG, LIVEDATA_RESULT)
        forEach {
            val viewModelParam = it.getViewModelParam()

            sendNoteMsg("==================> Create private LiveData") //创建私有属性
            generatePrivateProperty(
                propertyName = viewModelParam.priPropertyName,
                propertyType = mutableLiveData.parameterizedBy(
                    result.parameterizedBy(viewModelParam.responseBody)
                ),
                delegateValue = "${MUTABLE_LIVEDATA}()",
                addToClass = classBuilder,
                KModifier.PRIVATE
            )

            sendNoteMsg("==================> Create public LiveData") //创建公开属性
            generatePublicProperty(
                propertyName = viewModelParam.pubPropertyName,
                propertyType = liveData.parameterizedBy(result.parameterizedBy(viewModelParam.responseBody)),
                initValue = viewModelParam.priPropertyName,
                addToClass = classBuilder
            )
            finishBlock(it, requestDataList, viewModelParam)
        }
    }
}

private fun generatePublicProperty(
    propertyName: String,
    propertyType: ParameterizedTypeName,
    initValue: String,
    addToClass: TypeSpec.Builder,
    vararg modifier: KModifier,
) {
    addToClass.addProperty(
        PropertySpec.builder(propertyName, propertyType).initializer(initValue)
            .addModifiers(*modifier).build()
    )
}

private fun generatePrivateProperty(
    propertyName: String,
    propertyType: ParameterizedTypeName,
    delegateValue: String,
    addToClass: TypeSpec.Builder,
    vararg modifier: KModifier,
) {

    addToClass.addProperty(
        PropertySpec.builder(propertyName, propertyType).delegate("lazy{$delegateValue}")
            .addModifiers(*modifier).build()
    )
}

/**
 * 通过Annotation获取生成ViewModel所需参数
 * @return GenerateViewModelData
 */
private fun Annotation.getViewModelParam(): GenerateViewModelData {
    var requestClass = ""
    var responseClass = ""
    var pubPropertyName = ""
    when (this) {
        is BindLiveData -> {
            requestClass = getAnnotationValue { it.requestClass }.toString()
            responseClass = getAnnotationValue { bld -> bld.responseClass }.toString()
            pubPropertyName = liveDataName.ifEmpty { methodName.plus(LIVEDATA) }
        }
        is BindStateFlow -> {
            requestClass = getAnnotationValue { it.requestClass }.toString()
            responseClass = getAnnotationValue { bld -> bld.responseClass }.toString()
            pubPropertyName =
                stateFlowName.ifEmpty { methodName.plus(STATE_FLOW) }
        }
    }


    val priPropertyName = privateName(pubPropertyName)

    val requestPackage = splitPackage(asKotlinPackage(requestClass))
    val requestBody = ClassName(requestPackage.first, requestPackage.second)

    val responsePackage = splitPackage(asKotlinPackage(responseClass))
    val responseBody = ClassName(responsePackage.first, responsePackage.second)
    return GenerateViewModelData(requestBody, responseBody, pubPropertyName, priPropertyName)
}


/**
 * 获取注解参数为KClass<*>会出现异常时，通过异常获取返回值
 * @receiver BindLiveData
 * @return (Any..Any?)
 */
private fun BindLiveData.getAnnotationValue(block: (BindLiveData) -> KClass<*>) = try {
    block(this)
} catch (e: MirroredTypeException) {
    e.typeMirror
}


private fun BindStateFlow.getAnnotationValue(block: (BindStateFlow) -> KClass<*>) = try {
    block(this)
} catch (e: MirroredTypeException) {
    e.typeMirror
}

private fun privateName(name: String) = "_$name"




