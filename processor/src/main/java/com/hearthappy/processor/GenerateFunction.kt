package com.hearthappy.processor

import com.hearthappy.annotations.*
import com.hearthappy.processor.common.*
import com.hearthappy.processor.model.*
import com.hearthappy.processor.tools.asKotlinClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeSpec

internal fun generateFunctionByLiveData(it: BindLiveData, requestData: RequestData?, viewModelParam: ViewModelData, classBuilder: TypeSpec.Builder, requiredImport: MutableList<String>) {
    val function = FunSpec.builder(it.methodName).apply {
        generateMethodParametersSpec(requestData)
        generateMethodRequestScope(requestData, viewModelParam, requiredImport)
        addStatement("onSucceed = { body, response ->${viewModelParam.priPropertyName}.postValue(Result.Success(body,response${addOrder(requestData)}))},")
        addStatement("onFailure = { ${viewModelParam.priPropertyName}.postValue(Result.Failed(it${addOrder(requestData)}))},")
        addStatement("onThrowable = { ${viewModelParam.priPropertyName}.postValue(Result.Throwable(it${addOrder(requestData)}))}")
        addStatement(")")
    }
    classBuilder.addFunction(function.build())
}


internal fun generateFunctionByStateFlow(it: BindStateFlow, requestData: RequestData?, viewModelParam: ViewModelData, classBuilder: TypeSpec.Builder, requiredImport: MutableList<String>) {
    val function = FunSpec.builder(it.methodName).apply {
        generateMethodParametersSpec(requestData)
        addStatement("${viewModelParam.priPropertyName}.value = ${NETWORK_REQUEST_STATE}.LOADING")
        generateMethodRequestScope(requestData, viewModelParam, requiredImport)
        addStatement("onSucceed = { body,response-> ${viewModelParam.priPropertyName}.value = ${NETWORK_REQUEST_STATE}.SUCCEED(body,response${addOrder(requestData)}) },")
        addStatement("onFailure = { ${viewModelParam.priPropertyName}.value = ${NETWORK_REQUEST_STATE}.FAILED(it${addOrder(requestData)}) },")
        addStatement("onThrowable = { ${viewModelParam.priPropertyName}.value = ${NETWORK_REQUEST_STATE}.Throwable(it${addOrder(requestData)}) }")
        addStatement(")")
    }
    classBuilder.addFunction(function.build())
}

private fun addOrder(requestData: RequestData?): String {
    return requestData?.run { order }?.takeIf { it.isNotBlank() }?.run { ",$this" } ?: ""
}


private fun FunSpec.Builder.generateMethodRequestScope(requestData: RequestData?, viewModelParam: ViewModelData, requiredImport: MutableList<String>) { //没有@Request注解的请求
    requestData?.apply {
        addStatement("requestScope<${viewModelParam.responseBody.simpleName}>(io = {")
        addRequiredImport(requiredImport)

        streamingParameter?.let {
            requiredImport.add(NETWORK_DOWNLOAD)
            generateRequestApi(NETWORK_DOWNLOAD, http, url = url, headers = headers, fixedHeaders = fixedHeaders,cookies=cookies, serviceConfigData = serviceConfigData, listener = it.parameterName)
            addStatement("},")
            return@apply
        }
        multiPartParameters?.takeIf { it.isNotEmpty() }?.let {
            requiredImport.add(NETWORK_UPLOAD)
            generateRequestApi(NETWORK_UPLOAD, http, url = url, headers = headers, fixedHeaders = fixedHeaders,cookies=cookies, serviceConfigData = serviceConfigData, listener = it[1].parameterName, multipart = it[0].parameterName)
            addStatement("},")
            return@apply
        }

        requiredImport.add(NETWORK_REQUEST)
        generateRequestApi(NETWORK_REQUEST, http, requestBodyData.bodyType, url, headers, fixedHeaders,cookies, requestParameters, requestBodyData.jsonParameterName, requestBodyData.xwfParameters, serviceConfigData)
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
    if(cookies.isNotEmpty()) requiredImport.add(NETWORK_Cookie)
}

private fun FunSpec.Builder.generateRequestApi(sendApi: String, http: Http, bodyType: BodyType = BodyType.NONE, url: String, headers: List<HeaderData>? = null, fixedHeaders: List<String>?,cookies: List<CookieData>? = null, parameters: List<String>? = null, requestBody: Any? = null, appends: Pair<String, Map<String, String>>? = null, serviceConfigData: ServiceConfigData?, listener: String? = null, multipart: String? = null) {

    addStatement("$sendApi(")
    if (requestBody != Http.GET) addStatement("httpType=${http}")
    if (bodyType != BodyType.NONE) addStatement(",bodyType=${bodyType}")

    addStatement(",url=\"$url\"")
    listener?.apply { addStatement(",listener=$this") }
    if (headers?.isNotEmpty() == true || fixedHeaders?.isNotEmpty() == true) {
        addStatement(",headers = listOf(")
        fixedHeaders?.apply { for (fixedHeader in this) addStatement("Header(${fixedHeader.asFixedHeader()}),") }
        headers?.apply { for (header in this) addStatement("Header(\"${header.key}\",${header.parameterName}),") }
        addStatement(")")
    }

    // TODO: 动态cookie
    if(cookies?.isNotEmpty()==true){
        addStatement(",cookies = listOf(")
        cookies.apply { for (cookie in this) addStatement("Cookie(\"${cookie.key}\",${cookie.parameterName}),") }
        addStatement(")")
    }


    multipart?.apply { addStatement(",multipartBody=$this") }

    parameters?.apply {
        if (!this.contains(appends?.first) && this.isNotEmpty()) {
            addStatement(",parameters={")
            for (parameter in this) addStatement("parameter(\"${parameter}\", ${parameter})")
            addStatement("}")
        }
    }

    requestBody?.apply { addStatement(",requestBody=$requestBody") }

    appends?.apply {
        if (this.second.isNotEmpty()) {
            addStatement(",appends={")
            second.forEach { (queryName, queryValue) -> addStatement("append(\"${queryName}\", ${this.first}.${queryValue})") }
            addStatement("}")
        }
    }

    serviceConfigData?.apply { addStatement(",defaultConfig=app.${this.key}()") }
    addStatement(")")
}

/**
 * 生成方法参数
 * @receiver FunSpec.Builder
 * @param requestData RequestData?
 */
private fun FunSpec.Builder.generateMethodParametersSpec(requestData: RequestData?) { //没有@Request注解时，由开发者自定义请求
    requestData?.apply {
        methodParameters.apply {
            for (parameterData in this) addParameter(parameterData.parameterName, parameterData.parameterType.asKotlinClassName())
        }
        //添加下载进度监听
        streamingParameter?.apply {
            addParameter(parameterName, parameterType.asKotlinClassName())
        }
        //查找[multiPart,listener]中的listener参数，并添加上传进度监听
        multiPartParameters?.find { it.parameterType == KTOR_PROGRESS_PKG }?.apply { addParameter(parameterName, parameterType.asKotlinClassName()) }
    } ?: addParameter("io", LambdaTypeName.get(returnType = ClassName(KTOR_CLIENT_RESPONSE_PKG, "HttpResponse")).copy(suspending = true))
}
