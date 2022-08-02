package com.hearthappy.processor

import com.hearthappy.annotations.AndroidViewModel
import com.hearthappy.annotations.BindLiveData
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.processor.common.*
import com.hearthappy.processor.model.ServiceConfigData
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

internal fun ViewModelProcessor.generateAndroidViewModelFile(roundEnv: RoundEnvironment, androidViewModelElements: MutableSet<out Element>, generatedSource: String, serviceConfigList: List<ServiceConfigData>): Boolean {
    if (androidViewModelElements.isEmpty()) {
        return sendErrorMsg("The AndroidViewModel annotation was not found. Please declare AndroidViewModel annotations for Activity or Fragment")
    }

    //获取并封装所有请求
    val requestDataList = getRequestDataList(roundEnv, serviceConfigList)
    for (avmElement in androidViewModelElements) {
        val androidViewModelAnt = avmElement.getAnnotation(AndroidViewModel::class.java)
        val bindLiveDataAnt = avmElement.getAnnotationsByType(BindLiveData::class.java)
        val bindStateFlowAnt = avmElement.getAnnotationsByType(BindStateFlow::class.java)
        val viewModelClassName = androidViewModelAnt.viewModelClassName.ifEmpty {
            extractName(avmElement.simpleName.toString()).plus("ViewModel")
        }

        //当前ViewModel所需导入的包名
        val collectRequiredImport = mutableListOf<String>()

        sendNoteMsg("@AndroidViewModel className:${viewModelClassName},@BindLiveData count:${bindLiveDataAnt.size},@BindStateFlow count:${bindStateFlowAnt.size}") //创建类
        //        val classBuilder = builderViewModelClassSpec(viewModelClassName)
        val generateClass = generateClass(viewModelClassName, listOf(ParameterSpec("app", application, KModifier.PRIVATE)), superClassName = androidViewModel)


        //通过BindLiveData创建属性、方法
        generatePropertyAndMethodByLiveData(generateClass, requestDataList, bindLiveDataAnt) { bld, requestData, viewModelParam ->
            sendNoteMsg("==================> Create function: ${bld.methodName}") //通过类型别名创建函数参数
            generateFunctionByLiveData(bld, requestData, viewModelParam, generateClass, collectRequiredImport)
        }

        //通过BindStateFlow创建属性、方法
        generatePropertyAndMethodByStateFlow(generateClass, requestDataList, bindStateFlowAnt) { bsf, requestData, viewModelParam ->
            sendNoteMsg("==================> Create function: ${bsf.methodName}") //创建公开属性
            generateFunctionByStateFlow(bsf, requestData, viewModelParam, generateClass, collectRequiredImport)
        }

        //写入文件
        generateFileAndWrite(viewModelClassName, generateClass, generatedSource, serviceConfigList, collectRequiredImport)
    }
    return true
}


private fun extractName(className: String) = when {
    className.contains("Activity") -> className.substringBefore("Activity")
    className.contains("Fragment") -> className.substringBefore("Fragment")
    else -> className
}

internal fun ViewModelProcessor.generateFileAndWrite(viewModelClassName: String, generateClass: TypeSpec.Builder, generatedSource: String, serviceConfigList: List<ServiceConfigData>, collectRequiredImport: List<String>) { //创建文件

    val requiredImport = getFinallyRequiredImport(collectRequiredImport) //创建文件,导包并取别名import xxx.requestScopeX as RequestScope
    val file = FileSpec.builder(GENERATE_VIEWMODEL_PKG, viewModelClassName).apply {

        //                .addAliasedImport(requestScopeX, "RequestScope") //导包取别名
        //                .addTypeAlias(typeAlias).build() //文件内添加类型别名
        if (requiredImport.isNotEmpty()) {
            addImport(NETWORK_PKG, requiredImport)
            if(requiredImport.size>1){
                addImport(KTOR_CLIENT_REQUEST_PKG, KTOR_PARAMETER)
                //            addImport(KTOR_CLIENT_RESPONSE_PKG, HTTP_RESPONSE)
                addImport(KTOR_HTTP_PKG, KTOR_HTTP_RESPONSE, NETWORK_CONTENT_TYPE)
            }

            if (serviceConfigList.isNotEmpty()) {
                val map = serviceConfigList.map { it.key }
                addImport(GENERATE_CONFIG_PKG, map)
            }
        }
        addType(generateClass.build())
    }.build()

    file.writeTo(File(generatedSource))
    sendNoteMsg("==================> Create a $viewModelClassName file and write the class to the file")
}

/**
 * 过滤去重，并添加请求域包
 * @param collectRequiredImport List<String>
 * @return List<String>
 */
private fun getFinallyRequiredImport(collectRequiredImport: List<String>): List<String> {
    return collectRequiredImport.isNotEmpty().takeIf { it }?.run {
        collectRequiredImport.plus(NETWORK_REQUEST_SCOPE).plus(NETWORK_REQUEST).toSet().toList()
    } ?: run { collectRequiredImport }
}




