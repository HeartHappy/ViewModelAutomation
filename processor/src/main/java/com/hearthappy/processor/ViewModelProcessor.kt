package com.hearthappy.processor

import com.google.auto.service.AutoService
import com.hearthappy.annotations.*
import com.hearthappy.processor.log.errorMessage
import com.hearthappy.processor.log.noteMessage
import com.hearthappy.processor.model.*
import com.hearthappy.processor.tools.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

/**
 * @Author ChenRui
 * @Date   2021/12/27-16:46
 * @Email  1096885636@qq.com
 * ClassDescription :
 * kotlin poet:
 * liveData.parameterizedBy(responseBean):liveData：声明类型，responseBean：泛型类型
 * FunSpec.addTypeVariable(TypeVariableName("T")) : 函数泛型 ：fun <T> add()
 * FunSpec.addComment("AA"):为函数尾部添加注释  fun(){}  //AA
 * FunSpec.addKdoc("BB"):为函数顶部添加文本注释 /**BB*/ fun(){}
 * 1、支持基本的ViewModel类和LiveData与请求方法绑定，由开发者传入自定义的请求
 * 2、支持开发者自定义参数，自动生成网络请求
 * 3、支持添加请求头
 * 4、支持patch、delete
 * 5、支持rest请求
 * 6、全局基础URL，在使用中支持更改
 *
 * 7、支持代理、
 * 8、支持设置超时
 * 9、支持是否输出网络请求日志
 *
 * annotation:
 * sendNoteMsg(element.enclosedElements.toList().toString())
 * sendNoteMsg(element.enclosingElement.toString()) //获取包名：com.hearthappy.viewmodelautomation.model
 * sendNoteMsg(element.kind.name) //获取类型：CLASS
 * sendNoteMsg(element.simpleName.toString()) //获取类名
 * sendNoteMsg(element.asType().toString()) //获取类的全相对路径：com.hearthappy.viewmodelautomation.model.ReLogin
 */
@AutoService(Processor::class) class ViewModelProcessor : AbstractProcessor() { //导包所需

    private val application = ClassName(APPLICATION_PKG, APPLICATION)
    private val androidViewModel = ClassName(ANDROID_VIEW_MODEL_PKG, ANDROID_VIEW_MODEL)

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            AndroidViewModel::class.java.name,
            BindLiveData::class.java.name,
            BindStateFlow::class.java.name,
            Request::class.java.name,
            BaseConfig::class.java.name,
            Body::class.java.name
        )
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?,
    ): Boolean {
        return roundEnv?.processingOver()?.takeIf { it }
            ?.let { generatedFinish() } ?: processAnnotations(roundEnv)
    }

    private fun processAnnotations(
        roundEnv: RoundEnvironment?,
    ): Boolean {
        return roundEnv?.run {
            val androidViewModelElements =
                getElementsAnnotatedWith(AndroidViewModel::class.java) //            return handlerAndroidViewModelAnt(androidViewModelElements, this)
            val requestDataList = createRequestDataList(this)
            handlerAndroidViewModel(androidViewModelElements, requestDataList)
        } ?: run {
            sendErrorMsg("RoundEnvironment is null hence skip the process.")
        }
    }

    private fun handlerAndroidViewModel(
        androidViewModelElements: MutableSet<out Element>, requestDataList: List<RequestData>
    ): Boolean {
        if (androidViewModelElements.isEmpty()) {
            return sendErrorMsg("The AndroidViewModel annotation was not found. Please declare AndroidViewModel annotations for Activity or Fragment")
        }
        val generatedSource = processingEnv.options[KAPT_KOTLIN_GENERATED] ?: run {
            return sendErrorMsg("Can't find target source.")
        }
        androidViewModelElements.forEach { avmElement ->
            val androidViewModel = avmElement.getAnnotation(AndroidViewModel::class.java)
            val bindLiveData = avmElement.getAnnotationsByType(BindLiveData::class.java)
            val bindStateFlow = avmElement.getAnnotationsByType(BindStateFlow::class.java)
            val viewModelClassName = androidViewModel.viewModelClassName.ifEmpty {
                extractName(avmElement.simpleName.toString()).plus("ViewModel")
            }

            sendNoteMsg("@AndroidViewModel className:${viewModelClassName},@BindLiveData count:${bindLiveData.size},@BindStateFlow count:${bindStateFlow.size}") //创建类
            val classBuilder = builderViewModelClassSpec(viewModelClassName)

            //通过BindLiveData创建属性、方法
            generatePropertyAndMethodByLiveData(
                classBuilder, requestDataList, bindLiveData
            ) { bld, requestDataList, viewModelParam ->
                sendNoteMsg("==================> Create LiveData: ${bld.methodName} function") //通过类型别名创建函数参数
                generateFunctionByLiveData(bld, requestDataList, viewModelParam, classBuilder)
            }

            //通过BindStateFlow创建属性、方法
            generatePropertyAndMethodByStateFlow(
                classBuilder, requestDataList, bindStateFlow
            ) { bsf, requestDataList, viewModelParam ->
                sendNoteMsg("==================> Create StateFlow: ${bsf.methodName} function") //创建公开属性
                generateFunctionByStateFlow(bsf, requestDataList, viewModelParam, classBuilder)
            }

            //写入文件
            generateFileAndWrite(viewModelClassName, classBuilder, generatedSource)
        }
        return true
    }

    private fun builderViewModelClassSpec(viewModelClassName: String): TypeSpec.Builder {
        val classBuilder = TypeSpec.classBuilder(viewModelClassName)
        classBuilder.primaryConstructor(
            FunSpec.constructorBuilder().addParameter("app", application).build()
        ).addSuperclassConstructorParameter("app").superclass(androidViewModel)
        return classBuilder
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


    private fun generatedFinish(): Boolean {
        println("==================> build complete")
        return true
    }


    private fun String.asBaseUrlClassName(baseUrlPackagePath: String): ClassName {
        val splitPackage = splitPackage(asKotlinPackage(this))
        return ClassName(splitPackage.first, baseUrlPackagePath)
    }


    /*inline fun <reified T : Annotation> Element.getAnnotationKClass(block: T.() -> KClass<*>) = try {
        getAnnotation(T::class.java).block()
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }*/


    private fun extractName(className: String) = when {
        className.contains("Activity") -> className.substringBefore("Activity")
        className.contains("Fragment") -> className.substringBefore("Fragment")
        else -> className
    }


    /**
     * Create RequestData
     * @param roundEnv RoundEnvironment
     */
    private fun createRequestDataList(roundEnv: RoundEnvironment): List<RequestData> { //获取所有注解，将请求集中在一起
        val requestElements = roundEnv.getElementsAnnotatedWith(Request::class.java)
        val baseConfigElements = roundEnv.getElementsAnnotatedWith(BaseConfig::class.java)
        val headersElements = roundEnv.getElementsAnnotatedWith(Header::class.java)
        val fixedHeadersElements = roundEnv.getElementsAnnotatedWith(Headers::class.java)
        val bodyElements = roundEnv.getElementsAnnotatedWith(Body::class.java)
            .filterNot { it.enclosingElement.toString().contains("copy") }.toMutableSet()
        val queryElements = roundEnv.getElementsAnnotatedWith(Query::class.java)
            .filterNot { it.enclosingElement.toString().contains("copy") }.toMutableSet()

        //        outElementsAllLog(TAG_REQUEST, requestElements)
        //        outElementsAllLog(TAG_BASE_CONFIG, baseConfigElements) 
        //        outElementsAllLog(TAG_HEADER, headersElements)
        //        outElementsAllLog(TAG_BODY, bodyElements)
        //        outElementsAllLog(TAG_QUERY, queryElements)


        //创建请求集合
        val requestDataList = mutableListOf<RequestData>()
        requestElements.forEach { requestElement ->
            val requestAnt = requestElement.getAnnotation(Request::class.java)
            val baseConfigData = baseConfigElements.filterBaseUrlByRequestClass(
                requestAnt, requestElement.simpleName.toString()
            )
            val headerElements = headersElements.filterHeaderAntByRequestClass(requestElement)
            val headers = headerElements.map {
                HeaderData(it.getAnnotation(Header::class.java).value, it.simpleName.toString())
            }
            val requestClass = requestElement.simpleName.toString()
            val requestType = requestAnt.type
            val requestUrl = getRequestUrl(requestAnt, baseConfigData)

            //获取body相关参数
            val requestBodyData = getRequestBodyData(bodyElements, queryElements, requestElement)

            //查找固定headers请求头
            val fixedHeaders = getFixedHeaders(fixedHeadersElements, requestElement)

            sendNoteMsg("getRequestBodyData:${requestElement.simpleName},$requestBodyData")

            //获取方法参数
            val methodParameters =
                getMethodParameters(requestElement, bodyElements, requestBodyData)

            //获取get请求参数
            val requestParameters: List<String> =
                getRequestParameters(methodParameters, requestAnt, headers, requestBodyData)
            val requestData = RequestData(
                requestClass,
                requestType,
                requestUrl,
                baseConfigData,
                headers,
                fixedHeaders,
                methodParameters,
                requestParameters,
                requestBodyData
            )
            requestDataList.add(requestData)
            sendNoteMsg("【RequestData】:$requestData")
        }
        return requestDataList
    }


    /**
     * 获取固定Headers
     * @param fixedHeadersElements MutableSet<out Element>
     * @param requestElement Element
     * @return List<String>?
     */
    private fun getFixedHeaders(
        fixedHeadersElements: MutableSet<out Element>, requestElement: Element
    ) = fixedHeadersElements.find { it.simpleName == requestElement.simpleName }
        ?.getAnnotation(Headers::class.java)?.headers?.toList()


    /**
     * 获取请求Body的相关信息：参数名、以及Body数据类型，以及相关数据类型的参数结构
     * @param bodyElements MutableSet<out Element>
     * @param queryElements MutableSet<out Element>
     * @param requestElement Element
     * @return RequestBodyData
     */
    private fun getRequestBodyData(
        bodyElements: MutableSet<out Element>,
        queryElements: MutableSet<out Element>,
        requestElement: Element
    ): RequestBodyData { //获取当前请求的@Body元素
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
    private fun getCurrentBodyQueryMap(
        currentBodyElement: Element, queryElements: MutableSet<out Element>
    ): Map<String, String> {
        val queryMap = mutableMapOf<String, String>()
        queryElements.forEach { query -> //遍历当前Body相同类名的Query的属性值
            if (currentBodyElement.asType().toString()
                    .contains(query.enclosingElement.toString().removeWith("(", ")"))
            ) {
                val queryAnt = query.getAnnotation(Query::class.java)
                queryMap[queryAnt.value] = query.simpleName.toString()
            }
        }
        return queryMap
    }


    /**
     * 根据@Body中的数据类型创建所需参数
     * @param bodyType BodyType：X_WWW_FormUrlEncoded、JSON
     * @param currentBodyElement Element?
     * @return RequestBodyData
     */
    private fun createRequestBodyData(
        bodyType: BodyType, currentBodyElement: Element, queryElements: MutableSet<out Element>
    ): RequestBodyData {
        val currentBodyParameterName = getCurrentBodyParameterName(currentBodyElement)
        return if (bodyType == BodyType.X_WWW_FormUrlEncoded) { //创建XWF数据类型所需的参数@Query列表
            val currentBodyQueryMap = getCurrentBodyQueryMap(currentBodyElement, queryElements)
            if (currentBodyQueryMap.isEmpty()) sendErrorMsg("The request class is ${currentBodyElement.asType()}, the specified BodyType is X_WWW_FormUrlEncoded, but the Query annotation is not declared, resulting in an error")
            RequestBodyData(bodyType,
                xwfParameters = currentBodyParameterName?.run { Pair(this, currentBodyQueryMap) })
        } else { //创建Json数据类型的参数
            RequestBodyData(bodyType, jsonParameterName = currentBodyParameterName)
        }
    }


    /**
     * 获取当前Body元素
     * @param bodyElements MutableSet<out Element>
     * @param requestElement Element
     * @return Element?
     */
    private fun getCurrentBodyElement(
        bodyElements: MutableSet<out Element>, requestElement: Element
    ): Element? {
        return bodyElements.find {
            if (it.kind == ElementKind.CLASS) it.simpleName == requestElement.simpleName else it.enclosingElement.toString()
                .removeWith("(", ")") == requestElement.simpleName.toString()
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
                    this.simpleName.toString()
                        .replaceFirstChar { it.lowercase(Locale.getDefault()) }
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
     * @param requestBodyData: String 根据BodyType过滤的对应注解的参数（@Body、@X_WWW_FormUrlEncoded）
     * @return List<String>
     */
    private fun getRequestParameters(
        parameters: List<ParameterData>,
        requestAnt: Request,
        headers: List<HeaderData>,
        requestBodyData: RequestBodyData?
    ): List<String> {

        //过滤headers参数
        val filterHeaderParameters =
            (parameters.map { it.parameterName } subtract headers.map { it.parameterName }).toList()

        //过滤rest参数
        val filterRestParameters = filterHeaderParameters.filterRestParameters(requestAnt.urlString)

        //过滤body或x_www_formUrlEncoded参数
        return filterRestParameters.filter { it != requestBodyData?.jsonParameterName }
    }

    /**
     * 获取请求Url
     * @param requestAnt Request
     * @param baseConfigData BaseConfigData?
     * @return String
     */
    private fun getRequestUrl(requestAnt: Request, baseConfigData: BaseConfigData?): String {
        val url = requestAnt.urlString.asRest("{", "}") //处理全局URL

        return baseConfigData?.run {
            val propertyName =
                if (propertyName.contains("\$annotations")) propertyName.getBaseUrlPropertyName() else propertyName
            "\${${packagePath.asBaseUrlClassName(propertyName)}}".plus(url)
        } ?: url
    }


    /**
     * 获取方法参数列表，根据Class和Parameter注解
     * @param requestElement Element
     * @return List<ParameterData>
     */
    private fun getMethodParameters(
        requestElement: Element,
        bodyElements: MutableSet<out Element>,
        requestBodyData: RequestBodyData?
    ): List<ParameterData> {
        val parameters = mutableListOf<ParameterData>()
        when (requestBodyData?.bodyType) {
            BodyType.NONE -> {
                parameters.addAll(getAllParameterByRequestClass(requestElement))
            }
            BodyType.TEXT -> {
            }
            BodyType.JSON, BodyType.X_WWW_FormUrlEncoded -> {
                parameters.addAll(getMethodParameterByBodyKind(bodyElements, requestElement))
            }
            BodyType.HTML -> {
            }
            BodyType.XML -> {
            }
            BodyType.FORM_DATA -> {
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
    private fun getMethodParameterByBodyKind(
        bodyElements: MutableSet<out Element>, requestElement: Element
    ): List<ParameterData> {
        val filterBodyElements = bodyElements.filterBodyAntByRequestClass(requestElement)
        if (filterBodyElements.isEmpty()) {
            sendBodyNotFoundErrorMsg(requestElement)
        } else {
            filterBodyElements.forEach { bodyElement ->
                when (bodyElement.kind) {
                    ElementKind.CLASS -> {
                        return listOf(
                            ParameterData(
                                bodyElement.simpleName.toString()
                                    .replaceFirstChar { it.lowercase(Locale.getDefault()) },
                                bodyElement.asType().toString()
                            )
                        )
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
     * 过滤URL为Rest风格的参数
     * @receiver List<String>
     * @param restUrl String
     * @return List<String>
     */
    private fun List<String>.filterRestParameters(restUrl: String): List<String> =
        restUrl.findRest(this)

    /**
     * 查找当前请求的基础URL
     * @receiver MutableSet<out Element>
     * @param request Request
     */
    private fun MutableSet<out Element>.filterBaseUrlByRequestClass(
        request: Request, requestClass: String
    ): BaseConfigData? {
        val baseConfigElements =
            this.filter { it.getAnnotation(BaseConfig::class.java).key == request.baseUrlKey }
        return if (baseConfigElements.isNotEmpty()) {
            val baseConfigAnt = baseConfigElements[0].getAnnotation(BaseConfig::class.java)

            if (baseConfigElements.size > 1) {
                sendErrorMsg("point to ${baseConfigElements[1].simpleName}. The @BaseConfig key must be unique, please specify the key for the parameter baseUrlKey in the @Request annotation")
            } //            outElementLog(TAG_BASE_CONFIG, baseConfigElements[0])
            BaseConfigData(
                baseConfigAnt.key,
                baseConfigAnt.enableLog,
                baseConfigAnt.proxyIp,
                baseConfigAnt.proxyPort,
                baseConfigElements[0].simpleName.toString(),
                baseConfigElements[0].enclosingElement.toString()
            )
        } else { //@Request 注解中指定参数baseUrlKey 为 server 没有找到,请设置你的@BaseConfig并指定key为server
            sendErrorMsg("point to $requestClass. The parameter baseUrlKey specified in the @Request annotation is not found as ${request.baseUrlKey}, please set your @BaseConfig and specify the key as ${request.baseUrlKey}")
            null
        }
    }

    /**
     * 过滤注解参数
     * @receiver MutableSet<out Element>
     * @param requestElement Element 根据请求Class
     * @return List<Element>
     */
    private fun MutableSet<out Element>.filterHeaderAntByRequestClass(requestElement: Element): List<Element> {
        return this.filter {
            it.enclosingElement.toString()
                .removeWith("(", ")") == requestElement.simpleName.toString()
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

    internal fun sendNoteMsg(msg: String) {
        processingEnv.noteMessage { msg }
    }

    private fun sendErrorMsg(msg: String): Boolean {
        processingEnv.errorMessage { msg }
        return false
    }

    private fun sendBodyNotFoundErrorMsg(requestElement: Element) {
        sendErrorMsg("${requestElement.simpleName} For a Post request, the Body could not be found, the @Body annotation can be declared on a class or parameter")
    }

    @Suppress("unused") private fun outElementsAllLog(tag: Any, elements: MutableSet<out Element>) {
        for (element in elements) {
            outElementLog(tag, element)
        }
    }

    private fun outElementLog(tag: Any, element: Element?) {
        element ?: return
        val value = when (tag) {
            TAG_REQUEST -> Request::class.java
            TAG_HEADER -> Header::class.java
            TAG_BODY -> Body::class.java
            TAG_QUERY -> Query::class.java
            TAG_BASE_CONFIG -> BaseConfig::class.java
            else -> Request::class.java
        }

        sendNoteMsg("======================<Gorgeous dividing line>=======================")
        sendNoteMsg("$tag simpleName:${element.simpleName}")
        sendNoteMsg("$tag enclosedElements:${element.enclosedElements.toList()}")
        sendNoteMsg("$tag enclosingElement:${element.enclosingElement}")
        sendNoteMsg("$tag kind:${element.kind}")
        sendNoteMsg("$tag getAnnotation:${element.getAnnotation(value)}")
        sendNoteMsg("$tag modifiers:${element.modifiers}")
        sendNoteMsg("$tag asType:${element.asType()}")
        sendNoteMsg("$tag element:${element}")
        sendNoteMsg("-------------------------end--------------------------")
    }
}


