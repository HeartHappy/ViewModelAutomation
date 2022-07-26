package com.hearthappy.processor

import com.hearthappy.annotations.*
import com.hearthappy.processor.model.*
import com.hearthappy.processor.tools.asRest
import com.hearthappy.processor.tools.findRest
import com.hearthappy.processor.tools.removeWith
import com.hearthappy.processor.tools.substringMiddle
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

/**
 * Create RequestData
 * @param roundEnv RoundEnvironment
 */
internal fun ViewModelProcessor.getRequestDataList(roundEnv: RoundEnvironment, createServiceConfigList: List<ServiceConfigData>): List<RequestData> { //获取所有注解，将请求集中在一起
    val requestElements = roundEnv.getElementsAnnotatedWith(Request::class.java)
    val headersElements = roundEnv.getElementsAnnotatedWith(Header::class.java)
    val fixedHeadersElements = roundEnv.getElementsAnnotatedWith(Headers::class.java)
    val bodyElements = roundEnv.getElementsAnnotatedWith(Body::class.java).filterNot { it.enclosingElement.toString().contains("copy") }.toMutableSet()
    val queryElements = roundEnv.getElementsAnnotatedWith(Query::class.java).filterNot { it.enclosingElement.toString().contains("copy") }.toMutableSet()


    //        outElementsAllLog(TAG_REQUEST, requestElements)
    //        outElementsAllLog(TAG_HEADER, headersElements)
    //        outElementsAllLog(TAG_BODY, bodyElements)
    //        outElementsAllLog(TAG_QUERY, queryElements)


    //创建请求集合
    val requestDataList = mutableListOf<RequestData>()

    return requestDataList.apply {
        requestElements.forEach { requestElement ->
            val requestAnt = requestElement.getAnnotation(Request::class.java)
            val headerElements = headersElements.filterHeaderAntByRequestClass(requestElement)
            val headers = headerElements.map { HeaderData(it.getAnnotation(Header::class.java).value, it.simpleName.toString()) }
            val requestClass = requestElement.simpleName.toString()
            val httpType = requestAnt.type
            val findBaseConfig = findBaseConfig(createServiceConfigList, requestAnt)
            val requestUrl = getRequestUrl(requestAnt, findBaseConfig)

            //获取body相关参数
            val requestBodyData = getRequestBodyData(bodyElements, queryElements, requestElement)

            //查找固定headers请求头
            val fixedHeaders = getFixedHeaders(fixedHeadersElements, requestElement)

            //获取方法参数
            val methodParameters = getMethodParameters(requestElement, bodyElements, requestBodyData)

            //获取get请求参数
            val requestParameters: List<String> = getRequestParameters(methodParameters, requestAnt, headers, requestBodyData)
            val requestData = RequestData(requestClass, httpType, requestUrl, findBaseConfig, headers, fixedHeaders, methodParameters, requestParameters, requestBodyData)
            add(requestData)

            //sendNoteMsg("【RequestData】:$requestData")
        }
    }
}

/**
 * 获取固定Headers
 * @param fixedHeadersElements MutableSet<out Element>
 * @param requestElement Element
 * @return List<String>?
 */
private fun getFixedHeaders(fixedHeadersElements: MutableSet<out Element>, requestElement: Element) = fixedHeadersElements.find { it.simpleName == requestElement.simpleName }?.getAnnotation(Headers::class.java)?.headers?.toList()


/**
 * 获取方法参数列表，根据Class和Parameter注解
 * @param requestElement Element
 * @return List<ParameterData>
 */
private fun ViewModelProcessor.getMethodParameters(requestElement: Element, bodyElements: MutableSet<out Element>, requestBodyData: RequestBodyData?): List<ParameterData> {
    val parameters = mutableListOf<ParameterData>()
    when (requestBodyData?.bodyType) {
        BodyType.NONE -> {
            parameters.addAll(getAllParameterByRequestClass(requestElement))
        }
        BodyType.TEXT -> {
        }
        BodyType.JSON, BodyType.FormUrlEncoded -> {
            parameters.addAll(getMethodParameterByBodyKind(bodyElements, requestElement))
        }
        BodyType.HTML -> {
        }
        BodyType.XML -> {
        }
        BodyType.FORM_DATA -> {
            parameters.addAll(getMethodParameterByBodyKind(bodyElements, requestElement))
        }
        else -> {
        }
    }
    return parameters
}


/**
 * 获取方法所有参数，根据Post、Patch请求时的Body参数
 * @param bodyElements MutableSet<out Element>
 * @param requestElement Element
 * @return List<ParameterData>
 */
private fun ViewModelProcessor.getMethodParameterByBodyKind(bodyElements: MutableSet<out Element>, requestElement: Element): List<ParameterData> {
    val filterBodyElements = bodyElements.filterBodyAntByRequestClass(requestElement)
    if (filterBodyElements.isEmpty()) {
        sendBodyNotFoundErrorMsg(requestElement)
    } else {
        filterBodyElements.forEach { bodyElement ->
            when (bodyElement.kind) {
                ElementKind.CLASS -> {
                    return listOf(ParameterData(bodyElement.simpleName.toString().replaceFirstChar { it.lowercase(Locale.getDefault()) }, bodyElement.asType().toString()))
                }
                ElementKind.PARAMETER -> {
                    return getAllParameterByRequestClass(requestElement)
                }
                else -> Unit
            }
        }
    }
    return emptyList()
}

/**
 * 获取参数列表，根据Class,使用场景：请求为FormUrlEncoded、POST等使用@Body注解时并且声明在Class上
 * @param requestElement Element
 * @return List<ParameterData>
 */
private fun getAllParameterByRequestClass(requestElement: Element): List<ParameterData> {
    val params = mutableListOf<String>()
    for (ele in requestElement.enclosedElements) { //获取参数类型和参数数量
        val paramName = ele.simpleName.toString()
        if (paramName == "<init>" || paramName.contains("get") || paramName.first().isUpperCase()) continue

        if (ele.simpleName.toString() == "copy") {
            val substringMiddle = ele.toString().substringMiddle("(", ")", 1, 1)
            val split = substringMiddle.split(",")
            return params.mapIndexed { index, s -> ParameterData(s, split[index]) }
        } else { //添加参数名
            params.add(ele.simpleName.toString())
        }
    }
    return emptyList()
}

/**
 * 过滤URL为Rest风格的参数
 * @receiver List<String>
 * @param restUrl String
 * @return List<String>
 */
private fun List<String>.filterRestParameters(restUrl: String): List<String> = restUrl.findRest(this)


/**
 * 过滤注解参数
 * @receiver MutableSet<out Element>
 * @param requestElement Element 根据请求Class
 * @return List<Element>
 */
private fun MutableSet<out Element>.filterHeaderAntByRequestClass(requestElement: Element): List<Element> {
    return this.filter {
        it.enclosingElement.toString().removeWith("(", ")") == requestElement.simpleName.toString()
    }
}


/**
 * body注解：可能为Parameter类型，可能为Class类型
 * @receiver MutableSet<out Element>
 * @param requestElement Element
 * @return List<Element>
 */
private fun MutableSet<out Element>.filterBodyAntByRequestClass(requestElement: Element): List<Element> {

    //过滤为Parameter类型的body注解
    val bodyForParamAnt = this.filterHeaderAntByRequestClass(requestElement)
    if (bodyForParamAnt.isNotEmpty()) return bodyForParamAnt

    //如果没找到，则查找为Class注解
    return this.filter { it.asType() == requestElement.asType() }
}

/**
 * 获取当前Body元素
 * @param bodyElements MutableSet<out Element>
 * @param requestElement Element
 * @return Element?
 */
private fun getCurrentBodyElement(bodyElements: MutableSet<out Element>, requestElement: Element): Element? {
    return bodyElements.find {
        if (it.kind == ElementKind.CLASS) it.simpleName == requestElement.simpleName else it.enclosingElement.toString().removeWith("(", ")") == requestElement.simpleName.toString()
    } ?: run { //            sendBodyNotFoundErrorMsg(requestElement)
        null
    }
}

/**
 * 获取当前Body元素将要创建的方法参数名称
 * @param bodyElement Element?
 * @return String?
 */
private fun getCurrentBodyParameterName(bodyElement: Element?): String? { //获取参数名
    return bodyElement?.run {
        when (this.kind) {
            ElementKind.PARAMETER -> {
                this.simpleName.toString()
            }
            ElementKind.CLASS -> {
                this.simpleName.toString().replaceFirstChar { it.lowercase(Locale.getDefault()) }
            }
            else -> {
                null
            }
        }
    }
}

/**
 * 获取当前Body的数据类型
 * @param bodyElement Element
 * @return BodyType
 */
private fun getCurrentBodyType(bodyElement: Element): BodyType {
    return bodyElement.getAnnotation(Body::class.java).bodyType
}


/**
 * 获取请求时的参数,过滤注解类型的参数
 * @param parameters: List<ParameterData> 返回过滤注解参数的集合
 * @param requestAnt: Request 请求注解
 * @param headers: List<HeaderData> 需要过滤的@Header注解
 * @param requestBodyData: String 根据BodyType过滤的对应注解的参数（@Body、@FormUrlEncoded）
 * @return List<String>
 */
private fun getRequestParameters(parameters: List<ParameterData>, requestAnt: Request, headers: List<HeaderData>, requestBodyData: RequestBodyData?): List<String> {

    //过滤headers参数
    val filterHeaderParameters = (parameters.map { it.parameterName } subtract headers.map { it.parameterName }).toList()

    //过滤rest参数
    val filterRestParameters = filterHeaderParameters.filterRestParameters(requestAnt.urlString)

    //过滤body或x_www_formUrlEncoded参数
    return filterRestParameters.filter { it != requestBodyData?.jsonParameterName }
}

/**
 * 获取请求Url,baseURL+ urlString
 * @param requestAnt Request
 * @param findServiceConfig BaseConfigData?  查找到的BaseConfig
 * @return String 返回最终请求URL
 */
private fun getRequestUrl(requestAnt: Request, findServiceConfig: ServiceConfigData?): String {
    val url = requestAnt.urlString.asRest("{", "}") //处理全局URL
    return findServiceConfig?.run { "\${app.defaultConfig().baseURL}".plus(url) } ?: url
}

private fun findBaseConfig(serviceConfigData: List<ServiceConfigData>, requestAnt: Request): ServiceConfigData? {
    return serviceConfigData.find { it.key == requestAnt.serviceKey }
}

/**
 * 获取请求Body的相关信息：参数名、以及Body数据类型，以及相关数据类型的参数结构
 * @param bodyElements MutableSet<out Element>
 * @param queryElements MutableSet<out Element>
 * @param requestElement Element
 * @return RequestBodyData
 */
private fun ViewModelProcessor.getRequestBodyData(bodyElements: MutableSet<out Element>, queryElements: MutableSet<out Element>, requestElement: Element): RequestBodyData { //获取当前请求的@Body元素
    return getCurrentBodyElement(bodyElements, requestElement)?.run { //获取@Body类型
        val bodyType = getCurrentBodyType(this)
        createRequestBodyData(bodyType, this, queryElements)
    } ?: RequestBodyData(BodyType.NONE)
}

/**
 * 获取当前Body对应的类中@Query值
 * @param currentBodyElement Element
 * @param queryElements MutableSet<out Element>
 * @return List<String>
 */
private fun getCurrentBodyQueryMap(currentBodyElement: Element, queryElements: MutableSet<out Element>): Map<String, String> {
    val queryMap = mutableMapOf<String, String>()
    queryElements.forEach { query -> //遍历当前Body相同类名的Query的属性值
        if (currentBodyElement.asType().toString().contains(query.enclosingElement.toString().removeWith("(", ")"))) {
            val queryAnt = query.getAnnotation(Query::class.java)
            queryMap[queryAnt.value] = query.simpleName.toString()
        }
    }
    return queryMap
}


/**
 * 根据@Body中的数据类型创建所需参数
 * @param bodyType BodyType：FormUrlEncoded、JSON
 * @param currentBodyElement Element?
 * @return RequestBodyData
 */
private fun ViewModelProcessor.createRequestBodyData(bodyType: BodyType, currentBodyElement: Element, queryElements: MutableSet<out Element>): RequestBodyData {
    val currentBodyParameterName = getCurrentBodyParameterName(currentBodyElement)
    return if (bodyType == BodyType.FormUrlEncoded || bodyType == BodyType.FORM_DATA) { //创建XWF数据类型所需的参数@Query列表
        val currentBodyQueryMap = getCurrentBodyQueryMap(currentBodyElement, queryElements)
        if (currentBodyQueryMap.isEmpty()) sendErrorMsg("The request class is ${currentBodyElement.asType()}, the specified BodyType is FormUrlEncoded, but the Query annotation is not declared, resulting in an error")
        RequestBodyData(bodyType, xwfParameters = currentBodyParameterName?.run { Pair(this, currentBodyQueryMap) })
    } else { //创建Json数据类型的参数
        RequestBodyData(bodyType, jsonParameterName = currentBodyParameterName)
    }
}

private fun ViewModelProcessor.sendBodyNotFoundErrorMsg(requestElement: Element) {
    sendErrorMsg("${requestElement.simpleName} For a Post request, the Body could not be found, the @Body annotation can be declared on a class or parameter")
}