package com.hearthappy.processor

import com.hearthappy.annotations.BindLiveData
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.annotations.BodyType
import com.hearthappy.annotations.Http
import com.hearthappy.processor.common.KTOR_CLIENT_RESPONSE_PKG
import com.hearthappy.processor.common.NETWORK_HEADER
import com.hearthappy.processor.common.NETWORK_REQUEST_SCOPE
import com.hearthappy.processor.common.NETWORK_REQUEST_STATE
import com.hearthappy.processor.model.HeaderData
import com.hearthappy.processor.model.RequestData
import com.hearthappy.processor.model.ServiceConfigData
import com.hearthappy.processor.model.ViewModelData
import com.hearthappy.processor.tools.asKotlinClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeSpec

internal fun generateFunctionByLiveData(it: BindLiveData, requestData: RequestData?, viewModelParam: ViewModelData, classBuilder: TypeSpec.Builder, requiredImport: MutableList<String>) {
    val function = FunSpec.builder(it.methodName).apply {
        generateMethodParametersSpec(requestData)
        generateMethodRequestScope(requestData, viewModelParam, requiredImport)
        addStatement("onFailure = { ${viewModelParam.priPropertyName}.postValue(Result.Failed(it${addSite(requestData)}))},")
        addStatement("onSucceed = { body, _ ->${viewModelParam.priPropertyName}.postValue(Result.Success(body${addSite(requestData)}))},")
        addStatement("onThrowable = { ${viewModelParam.priPropertyName}.postValue(Result.Throwable(it${addSite(requestData)}))}")
        addStatement(")")
    }
    classBuilder.addFunction(function.build())
}

internal fun generateFunctionByStateFlow(it: BindStateFlow, requestData: RequestData?, viewModelParam: ViewModelData, classBuilder: TypeSpec.Builder, requiredImport: MutableList<String>) {
    val function = FunSpec.builder(it.methodName).apply {
        generateMethodParametersSpec(requestData)
        addStatement("${viewModelParam.priPropertyName}.value = ${NETWORK_REQUEST_STATE}.LOADING")
        generateMethodRequestScope(requestData, viewModelParam, requiredImport)
        addStatement("onFailure = { ${viewModelParam.priPropertyName}.value = ${NETWORK_REQUEST_STATE}.FAILED(it${addSite(requestData)}) },")
        addStatement("onSucceed = { body,response-> ${viewModelParam.priPropertyName}.value = ${NETWORK_REQUEST_STATE}.SUCCEED(body,response${addSite(requestData)}) },")
        addStatement("onThrowable = { ${viewModelParam.priPropertyName}.value = ${NETWORK_REQUEST_STATE}.Throwable(it${addSite(requestData)}) }")
        addStatement(")")
    }
    classBuilder.addFunction(function.build())
}

private fun addSite(requestData: RequestData?): String {
    return requestData?.run { order }?.takeIf { it.isNotBlank() }?.run { ",$this" } ?: ""
}

private fun FunSpec.Builder.generateMethodRequestScope(requestData: RequestData?, viewModelParam: ViewModelData, requiredImport: MutableList<String>) { //没有@Request注解的请求
    requestData?.apply {
        addStatement("requestScope<${viewModelParam.responseBody.simpleName}>(io = {")
        addRequiredImport(requiredImport)
        generateRequestApi(http, requestBodyData.bodyType, url, headers, fixedHeaders, requestParameters, requestBodyData.jsonParameterName, requestBodyData.xwfParameters, serviceConfigData)
        addStatement("},")
    } ?: let {
        requiredImport.add(NETWORK_REQUEST_SCOPE)
        addStatement("requestScope<${viewModelParam.responseBody.simpleName}>(io = io,")
    }
}

private fun RequestData.addRequiredImport(requiredImport: MutableList<String>) {
    requiredImport.add(http.name)
    requiredImport.add(requestBodyData.bodyType.name)
    if (headers.isNotEmpty() || fixedHeaders?.isNotEmpty() == true) requiredImport.add(NETWORK_HEADER)
}

private fun FunSpec.Builder.generateRequestApi(http: Http, bodyType: BodyType, url: String, headers: List<HeaderData>? = null, fixedHeaders: List<String>?, parameters: List<String>? = null, requestBody: Any? = null, appends: Pair<String, Map<String, String>>? = null, serviceConfigData: ServiceConfigData?) {

    addStatement("sendKtorRequest(")
    if (requestBody != Http.GET) addStatement("httpType=${http}")
    if (bodyType != BodyType.NONE) addStatement(",bodyType=${bodyType}")

    addStatement(",url=\"$url\"")

    if (headers?.isNotEmpty() == true || fixedHeaders?.isNotEmpty() == true) {
        addStatement(",headers = listOf(")
        fixedHeaders?.apply { for (fixedHeader in this) addStatement("Header(${fixedHeader.asFixedHeader()}),") }
        headers?.apply { for (header in this) addStatement("Header(\"${header.key}\",${header.parameterName}),") }
        addStatement(")")
    }


    if (parameters?.isNotEmpty() == true && !parameters.contains(appends?.first)) {
        parameters.apply {
            addStatement(",parameters={")
            for (parameter in this) addStatement("parameter(\"${parameter}\", ${parameter})")
            addStatement("}")
        }
    }

    requestBody?.apply { addStatement(",requestBody=$requestBody") }

    if (appends?.second?.isNotEmpty() == true) {
        appends.apply {
            addStatement(",appends={")
            second.forEach { (queryName, queryValue) -> addStatement("append(\"${queryName}\", ${this.first}.${queryValue})") }
            addStatement("}")
        }
    }
    serviceConfigData?.apply { addStatement(",defaultConfig=app.${this.key}()") }
    addStatement(")")
}

private fun FunSpec.Builder.generateMethodParametersSpec(requestData: RequestData?) { //没有@Request注解时，由开发者自定义请求
    //生成方法参数
    requestData?.methodParameters?.apply {
        for (parameterData in this) addParameter(parameterData.parameterName, parameterData.parameterType.asKotlinClassName())
    } ?: addParameter("io", LambdaTypeName.get(returnType = ClassName(KTOR_CLIENT_RESPONSE_PKG, "HttpResponse")).copy(suspending = true))
}
