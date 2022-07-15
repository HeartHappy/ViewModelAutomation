package com.hearthappy.processor

import com.hearthappy.annotations.BindLiveData
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.processor.common.*
import com.hearthappy.processor.model.ViewModelData
import com.hearthappy.processor.model.RequestData
import com.hearthappy.processor.tools.asKotlinPackage
import com.hearthappy.processor.tools.splitPackage
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.KClass

internal fun ViewModelProcessor.generatePropertyAndMethodByStateFlow(
    classBuilder: TypeSpec.Builder,
    requestDataList: List<RequestData>,
    bindStateFlow: Array<BindStateFlow>?,
    finishBlock: (BindStateFlow, List<RequestData>, ViewModelData) -> Unit
) {
    bindStateFlow?.onEach {
        val viewModelParam = it.getViewModelParam()

        sendNoteMsg("==================> Create a private ${viewModelParam.priPropertyName}")

        val generateMutableStateFlow = generateDelegatePropertySpec(
            viewModelParam.priPropertyName, mutableStateFlow.parameterizedBy(
                requestState.parameterizedBy(viewModelParam.responseBody)
            ), "${MUTABLE_STATE_FLOW}(${KTOR_REQUEST_STATE}.DEFAULT)", KModifier.PRIVATE
        )

        classBuilder.addProperty(generateMutableStateFlow)

        sendNoteMsg("==================> Create a public ${viewModelParam.pubPropertyName}") //创建公开属性
        val generateStateFlow = generatePropertySpec(
            viewModelParam.pubPropertyName, stateFlow.parameterizedBy(
                requestState.parameterizedBy(viewModelParam.responseBody)
            ), viewModelParam.priPropertyName
        )

        classBuilder.addProperty(generateStateFlow)

        finishBlock(it, requestDataList, viewModelParam)
    }
}


internal fun ViewModelProcessor.generatePropertyAndMethodByLiveData(
    classBuilder: TypeSpec.Builder,
    requestDataList: List<RequestData>,
    bindLiveData: Array<BindLiveData>?,
    finishBlock: (BindLiveData, List<RequestData>, ViewModelData) -> Unit
) {
    bindLiveData?.onEach {
        val viewModelParam = it.getViewModelParam()

        sendNoteMsg("==================> Create a private ${viewModelParam.priPropertyName}") //创建私有属性
        val generateMutableLiveData = generateDelegatePropertySpec(
            viewModelParam.priPropertyName, mutableLiveData.parameterizedBy(
                result.parameterizedBy(viewModelParam.responseBody)
            ), "${MUTABLE_LIVEDATA}()", KModifier.PRIVATE
        )
        classBuilder.addProperty(generateMutableLiveData)

        sendNoteMsg("==================> Create a public ${viewModelParam.pubPropertyName}") //创建公开属性
        val generateLiveData = generatePropertySpec(
            viewModelParam.pubPropertyName,
            liveData.parameterizedBy(result.parameterizedBy(viewModelParam.responseBody)),
            viewModelParam.priPropertyName
        )
        classBuilder.addProperty(generateLiveData)

        finishBlock(it, requestDataList, viewModelParam)
    }
}


/**
 * 通过Annotation获取生成ViewModel所需参数
 * @return GenerateViewModelData
 */
private fun Annotation.getViewModelParam(): ViewModelData {
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
            pubPropertyName = stateFlowName.ifEmpty { methodName.plus(STATE_FLOW) }
        }
    }


    val priPropertyName = privateName(pubPropertyName)

    val requestPackage = splitPackage(asKotlinPackage(requestClass))
    val requestBody = ClassName(requestPackage.first, requestPackage.second)

    val responsePackage = splitPackage(asKotlinPackage(responseClass))
    val responseBody = ClassName(responsePackage.first, responsePackage.second)
    return ViewModelData(requestBody, responseBody, pubPropertyName, priPropertyName)
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




