package com.hearthappy.processor

import com.hearthappy.annotations.BindLiveData
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.annotations.BodyType
import com.hearthappy.annotations.RequestType
import com.hearthappy.processor.common.KTOR_CLIENT_RESPONSE_PKG
import com.hearthappy.processor.common.KTOR_REQUEST_STATE
import com.hearthappy.processor.model.*
import com.hearthappy.processor.tools.asKotlinClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeSpec

internal fun generateFunctionByLiveData(it: BindLiveData, requestDataList: List<RequestData>, viewModelParam: GenerateViewModelData, classBuilder: TypeSpec.Builder, requiredImport: MutableList<String>) {
    val function = FunSpec.builder(it.methodName).apply {
        generateMethodParametersSpec(requestDataList, viewModelParam)
        generateMethodRequestScope(requestDataList, viewModelParam,requiredImport)
        addStatement("onFailure = { ${viewModelParam.priPropertyName}.postValue(Result.Error(it))},")
        addStatement("onSucceed = { body, _ ->${viewModelParam.priPropertyName}.postValue(Result.Success(body))},")
        addStatement("onThrowable = { ${viewModelParam.priPropertyName}.postValue(Result.Throwable(it))}")
        addStatement(")")
    }
    classBuilder.addFunction(function.build())
}


internal fun generateFunctionByStateFlow(it: BindStateFlow, requestDataList: List<RequestData>, viewModelParam: GenerateViewModelData, classBuilder: TypeSpec.Builder, requiredImport: MutableList<String>) {
    val function = FunSpec.builder(it.methodName).apply {
        generateMethodParametersSpec(requestDataList, viewModelParam)
        addStatement("${viewModelParam.priPropertyName}.value = ${KTOR_REQUEST_STATE}.LOADING")
        generateMethodRequestScope(requestDataList, viewModelParam,requiredImport)
        addStatement("onFailure = { ${viewModelParam.priPropertyName}.value = ${KTOR_REQUEST_STATE}.FAILED(it) },")
        addStatement("onSucceed = { body,response-> ${viewModelParam.priPropertyName}.value = ${KTOR_REQUEST_STATE}.SUCCEED(body,response) },")
        addStatement("onThrowable = { ${viewModelParam.priPropertyName}.value = ${KTOR_REQUEST_STATE}.Throwable(it) }")
        addStatement(")")
    }
    classBuilder.addFunction(function.build())
}

private fun FunSpec.Builder.generateMethodRequestScope(requestDataList: List<RequestData>, viewModelParam: GenerateViewModelData,requiredImport:MutableList<String>) {
    //没有@Request注解的请求
    if (requestDataList.isEmpty()) {
        addStatement("requestScope<${viewModelParam.responseBody.simpleName}>(io = io,")
    } else {

        //拥有@Request注解请求
        val findRequestData = requestDataList.find { it.requestClass == viewModelParam.requestBody.simpleName }
        addStatement("requestScope<${viewModelParam.responseBody.simpleName}>(io = {")
        findRequestData?.apply {
            generateRequestApi(requestType, requestBodyData.bodyType, url, headers, fixedHeaders, requestParameters, requestBodyData.jsonParameterName, requestBodyData.xwfParameters, serviceConfigData)
            requiredImport.add(requestType.name)
            requiredImport.add(requestBodyData.bodyType.name)
        }
        addStatement("},")
    }
}


/**
 * 生成网络请求接口
 * @receiver FunSpec.Builder
 * @param requestType RequestType
 * @param bodyType BodyType
 * @param url String
 * @param headers List<HeaderData>?
 * @param parameters List<String>?
 * @param requestBody Any?
 * @param appends Pair<String, Map<String, String>>?
 */
private fun FunSpec.Builder.generateRequestApi(requestType: RequestType, bodyType: BodyType, url: String, headers: List<HeaderData>? = null, fixedHeaders: List<String>?, parameters: List<String>? = null, requestBody: Any? = null, appends: Pair<String, Map<String, String>>? = null, serviceConfigData: ServiceConfigData?) {

    addStatement("sendKtorRequest<HttpResponse>(requestType=${requestType},bodyType=${bodyType},url=\"$url\"")

    //    if (headers?.isNotEmpty() == true) {
    addStatement(",headers={")
    fixedHeaders?.forEach { addStatement("header(${it.asFixedHeader()})") } ?: addStatement("header($Application_Json)")
    headers?.forEach { header -> addStatement("header(\"${header.key}\",${header.parameterName})") }
    addStatement("}") //    }

    if (parameters?.isNotEmpty() == true && !parameters.contains(appends?.first)) {
        parameters.apply {
            addStatement(",parameters={")
            forEach { parameter -> addStatement("parameter(\"${parameter}\", ${parameter})") }
            addStatement("}")
        }
    }

    requestBody?.apply {
        addStatement(",requestBody=$requestBody")
    }

    if (appends?.second?.isNotEmpty() == true) {
        appends.apply {
            addStatement(",appends={")
            second.forEach { (queryName, queryValue) -> addStatement("append(\"${queryName}\", ${this.first}.${queryValue})") }
            addStatement("}")
        }
    }
    serviceConfigData?.let {
        addStatement(",defaultConfig=app.${it.key}()")
    }
    addStatement(")")
}


private fun FunSpec.Builder.generateMethodParametersSpec(requestDataList: List<RequestData>, viewModelParam: GenerateViewModelData) { //没有@Request注解时，由开发者自定义请求
    if (requestDataList.isEmpty()) {
        addParameter("io", LambdaTypeName.get(returnType = ClassName(KTOR_CLIENT_RESPONSE_PKG, "HttpResponse")).copy(suspending = true))
    } else { //有@Request注解时，自动生成响应请求
        requestDataList.find { it.requestClass == viewModelParam.requestBody.simpleName }?.methodParameters?.forEach {
            addParameter(it.parameterName, it.parameterType.asKotlinClassName())
        }
    }
}
