package com.hearthappy.processor

import com.google.auto.service.AutoService
import com.hearthappy.annotations.*
import com.hearthappy.processor.log.errorMessage
import com.hearthappy.processor.log.noteMessage
import com.hearthappy.processor.model.*
import com.hearthappy.processor.tools.asRest
import com.hearthappy.processor.tools.findRest
import com.hearthappy.processor.tools.removeWith
import com.hearthappy.processor.tools.substringMiddle
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.KClass

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
@AutoService(Processor::class) class ViewModelProcessor: AbstractProcessor() { //导包所需

    private val application = ClassName(APPLICATION_PKG, APPLICATION)
    private val androidViewModel = ClassName(ANDROID_VIEW_MODEL_PKG, ANDROID_VIEW_MODEL)

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(AndroidViewModel::class.java.name, BindLiveData::class.java.name, BindStateFlow::class.java.name, Request::class.java.name, BaseConfig::class.java.name, Body::class.java.name)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?,
    ): Boolean {
        return roundEnv?.processingOver()?.takeIf { it }?.let { generatedFinish() } ?: processAnnotations(roundEnv)
    }

    private fun processAnnotations(
        roundEnv: RoundEnvironment?,
    ): Boolean {
        return roundEnv?.run {
            val androidViewModelElements = getElementsAnnotatedWith(AndroidViewModel::class.java) //            return handlerAndroidViewModelAnt(androidViewModelElements, this)
            val requestDataList = createRequestDataList(this)
            handlerAndroidViewModel(androidViewModelElements, requestDataList)
        } ?: run {
            sendErrorMsg("RoundEnvironment is null hence skip the process.")
        }
    }

    private fun handlerAndroidViewModel(androidViewModelElements: MutableSet<out Element>, requestDataList: List<RequestData>): Boolean {
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
            generatePropertyAndMethodByLiveData(classBuilder, requestDataList, bindLiveData)

            //通过BindStateFlow创建属性、方法
            generatePropertyAndMethodByStateFlow(classBuilder, requestDataList, bindStateFlow)

            //写入文件
            generateFileAndWrite(viewModelClassName, classBuilder, generatedSource)

        }
        return true
    }

    private fun generatePropertyAndMethodByStateFlow(classBuilder: TypeSpec.Builder, requestDataList: List<RequestData>, bindStateFlow: Array<BindStateFlow>?) {
        bindStateFlow?.apply {
            val mutableStateFlow = ClassName(STATE_FLOW_PKG, MUTABLE_STATE_FLOW)
            val stateFlow = ClassName(STATE_FLOW_PKG, STATE_FLOW)
            val requestState = ClassName(KTOR_NETWORK_PKG, KTOR_REQUEST_STATE)
            forEach {
                val viewModelParam = it.getViewModelParam()
                sendNoteMsg("==================> Create a private StateFlow")
                generatePrivateProperty(propertyName = viewModelParam.priPropertyName, propertyType = mutableStateFlow.parameterizedBy(requestState), delegateValue = "$MUTABLE_STATE_FLOW($KTOR_REQUEST_STATE.DEFAULT)", addToClass = classBuilder, KModifier.PRIVATE)

                sendNoteMsg("==================> Create a public StateFlow") //创建公开属性
                generatePublicProperty(propertyName = viewModelParam.pubPropertyName, propertyType = stateFlow.parameterizedBy(requestState), initValue = viewModelParam.priPropertyName, addToClass = classBuilder)

                sendNoteMsg("==================> Create StateFlow: ${it.methodName} function") //创建公开属性
                generateFunctionByStateFlow(it, requestDataList, viewModelParam, classBuilder)
            }
        }
    }


    private fun generatePropertyAndMethodByLiveData(classBuilder: TypeSpec.Builder, requestDataList: List<RequestData>, bindLiveData: Array<BindLiveData>?) {
        bindLiveData?.apply {
            val mutableLiveData = ClassName(LIVEDATA_PKG, MUTABLE_LIVEDATA)
            val liveData = ClassName(LIVEDATA_PKG, LIVEDATA)
            val result = ClassName(KTOR_NETWORK_PKG, LIVEDATA_RESULT)
            forEach {
                val viewModelParam = it.getViewModelParam()

                sendNoteMsg("==================> Create private LiveData") //创建私有属性
                generatePrivateProperty(propertyName = viewModelParam.priPropertyName, propertyType = mutableLiveData.parameterizedBy(result.parameterizedBy(viewModelParam.responseBody)), delegateValue = "$MUTABLE_LIVEDATA()", addToClass = classBuilder, KModifier.PRIVATE)

                sendNoteMsg("==================> Create public LiveData") //创建公开属性
                generatePublicProperty(propertyName = viewModelParam.pubPropertyName, propertyType = liveData.parameterizedBy(result.parameterizedBy(viewModelParam.responseBody)), initValue = viewModelParam.priPropertyName, addToClass = classBuilder)

                sendNoteMsg("==================> Create LiveData: ${it.methodName} function") //通过类型别名创建函数参数
                generateFunctionByLiveData(it, requestDataList, viewModelParam, classBuilder)
            }
        }
    }

    private fun generateFunctionByLiveData(it: BindLiveData, requestDataList: List<RequestData>, viewModelParam: GenerateViewModelData, classBuilder: TypeSpec.Builder) {
        val function = FunSpec.builder(it.methodName).apply {
            generateMethodParametersSpec(requestDataList, viewModelParam)
            generateMethodRequestScope(requestDataList, viewModelParam)
            addStatement("onFailure = { ${viewModelParam.priPropertyName}.postValue(Result.Error(it))},")
            addStatement("onSucceed = { ${viewModelParam.priPropertyName}.postValue(Result.Success(it))},")
            addStatement("onThrowable = { ${viewModelParam.priPropertyName}.postValue(Result.Throwable(it))}")
            addStatement(")")
        }
        classBuilder.addFunction(function.build())
    }


    private fun generateFunctionByStateFlow(it: BindStateFlow, requestDataList: List<RequestData>, viewModelParam: GenerateViewModelData, classBuilder: TypeSpec.Builder) {
        val function = FunSpec.builder(it.methodName).apply {
            generateMethodParametersSpec(requestDataList, viewModelParam)
            addStatement("${viewModelParam.priPropertyName}.value = $KTOR_REQUEST_STATE.LOADING")
            generateMethodRequestScope(requestDataList, viewModelParam)
            addStatement("onFailure = { ${viewModelParam.priPropertyName}.value = $KTOR_REQUEST_STATE.FAILED(it) },")
            addStatement("onSucceed = { ${viewModelParam.priPropertyName}.value = $KTOR_REQUEST_STATE.SUCCEED(it) },")
            addStatement("onThrowable = { ${viewModelParam.priPropertyName}.value = $KTOR_REQUEST_STATE.Throwable(it) }")
            addStatement(")")
        }
        classBuilder.addFunction(function.build())
    }


    private fun FunSpec.Builder.generateMethodRequestScope(requestDataList: List<RequestData>, viewModelParam: GenerateViewModelData) {
        if (requestDataList.isEmpty()) {
            addStatement("requestScope<${viewModelParam.responseBody}>(io = io,")
        } else {
            val findRequestData = requestDataList.find { it.requestClass == viewModelParam.requestBody.simpleName }
            addStatement("requestScope<${viewModelParam.responseBody}>(io = {")

            findRequestData?.apply {
                generateRequestApi(requestType, requestBodyData.bodyType, url, headers, requestParameters, requestBodyData.jsonParameterName, requestBodyData.xwfParameters, baseConfigData)
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
    private fun FunSpec.Builder.generateRequestApi(requestType: RequestType, bodyType: BodyType, url: String, headers: List<HeaderData>? = null, parameters: List<String>? = null, requestBody: Any? = null, appends: Pair<String, Map<String, String>>? = null, baseConfigData: BaseConfigData?) {

        addStatement("sendKtorRequest<HttpResponse>(requestType=${requestType},bodyType=${bodyType},url=\"$url\"")
        if (headers?.isNotEmpty() == true) {
            headers.apply {
                addStatement(",headers={")
                forEach { header -> addStatement("header(\"${header.key}\",${header.parameterName})") }
                addStatement("}")
            }
        }

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
        baseConfigData?.let {
            if (!it.enabledLog) {
                addStatement(",enableLog=${it.enabledLog}")
            } // TODO: 需要修改成动态代理 ，现在是静态代理
            if (it.proxyIp.isNotEmpty() && it.proxyPort != -1) {
                addStatement(",proxyIp=\"${it.proxyIp}\",proxyPort=${it.proxyPort}")
            }
        }
        addStatement(")")
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
                pubPropertyName = stateFlowName.ifEmpty { methodName.plus(STATE_FLOW) }
            }
        }


        val priPropertyName = privateName(pubPropertyName)

        val requestPackage = splitPackage(asKotlinPackage(requestClass))
        val requestBody = ClassName(requestPackage.first, requestPackage.second)

        val responsePackage = splitPackage(asKotlinPackage(responseClass))
        val responseBody = ClassName(responsePackage.first, responsePackage.second)
        return GenerateViewModelData(requestBody, responseBody, pubPropertyName, priPropertyName)
    }

    private fun FunSpec.Builder.generateMethodParametersSpec(requestDataList: List<RequestData>, viewModelParam: GenerateViewModelData) { //没有@Request注解时，由开发者自定义请求
        if (requestDataList.isEmpty()) {
            addParameter("io", LambdaTypeName.get(returnType = viewModelParam.responseBody).copy(suspending = true))
        } else { //有@Request注解时，自动生成响应请求
            requestDataList.find { it.requestClass == viewModelParam.requestBody.simpleName }?.methodParameters?.forEach {
                addParameter(it.parameterName, it.parameterType.asKotlinClassName())
            }
        }
    }

    private fun builderViewModelClassSpec(viewModelClassName: String): TypeSpec.Builder {
        val classBuilder = TypeSpec.classBuilder(viewModelClassName)
        classBuilder.primaryConstructor(FunSpec.constructorBuilder().addParameter("app", application).build()).addSuperclassConstructorParameter("app").superclass(androidViewModel)
        return classBuilder
    }

    // TODO:优化所需导入的响应类，根据响应类型的包名进行遍历导包
    private fun generateFileAndWrite(viewModelClassName: String, classBuilder: TypeSpec.Builder, generatedSource: String) { //创建文件
        //创建文件,导包并取别名import xxx.requestScopeX as RequestScope
        sendNoteMsg("==================> Create a file and write the class to the file")
        val packageName = "com.hearthappy.compiler"
        val file = FileSpec.builder(packageName, viewModelClassName)

            //                .addAliasedImport(requestScopeX, "RequestScope") //导包取别名
            //                .addTypeAlias(typeAlias).build() //文件内添加类型别名
            .addImport(KTOR_NETWORK_PKG, KTOR_REQUEST_SCOPE, KTOR_REQUEST, GET, POST, PATCH, DELETE, NONE, TEXT, HTML, XML, JSON, FORM_DATA, X_WWW_FormUrlEncoded) //            .addImport(KTOR_NETWORK_PKG,"*")
            .addImport(KTOR_CLIENT_REQUEST_PKG, KTOR_PARAMETER, KTOR_HEADER).addImport(KTOR_CLIENT_RESPONSE_PKG, HTTP_RESPONSE) //            .addImport(KTOR_CLIENT_RESPONSE_PKG)
            .addType(classBuilder.build()).build()


        file.writeTo(File(generatedSource))
    }


    /**
     * 获取参数列表，根据Class注解,使用场景：请求为GET、FormUrlEncoded、POST（@Body注解声明在Class上）
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


    private fun generatePublicProperty(
        propertyName: String,
        propertyType: ParameterizedTypeName,
        initValue: String,
        addToClass: TypeSpec.Builder,
        vararg modifier: KModifier,
    ) {
        addToClass.addProperty(PropertySpec.builder(propertyName, propertyType).initializer(initValue).addModifiers(*modifier).build())
    }

    private fun generatePrivateProperty(
        propertyName: String,
        propertyType: ParameterizedTypeName,
        delegateValue: String,
        addToClass: TypeSpec.Builder,
        vararg modifier: KModifier,
    ) {
        addToClass.addProperty(PropertySpec.builder(propertyName, propertyType).delegate("lazy{$delegateValue}").addModifiers(*modifier).build())
    }


    private fun generatedFinish(): Boolean {
        println("==================> build complete")
        return true
    }


    private fun privateName(name: String) = "_$name"


    /**
     * 拆分包，将全类名分割成包和类
     * @param fullClassName String
     * @return Pair<String, String>
     */
    private fun splitPackage(fullClassName: String): Pair<String, String> {
        val lastIndexOf = fullClassName.lastIndexOf(".")
        val packageName = fullClassName.subSequence(0, lastIndexOf)
        val className = fullClassName.subSequence(lastIndexOf + 1, fullClassName.length)
        return Pair(packageName.toString(), className.toString())
    }

    private fun String.asKotlinClassName(): ClassName {
        val splitPackage = splitPackage(asKotlinPackage(this))
        return ClassName(splitPackage.first, splitPackage.second)
    }

    private fun String.asBaseUrlClassName(baseUrlPackagePath: String): ClassName {
        val splitPackage = splitPackage(asKotlinPackage(this))
        return ClassName(splitPackage.first, baseUrlPackagePath)
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

    /*inline fun <reified T : Annotation> Element.getAnnotationKClass(block: T.() -> KClass<*>) = try {
        getAnnotation(T::class.java).block()
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }*/

    private fun asKotlinPackage(javaPackage: String) = when (javaPackage) {
        in "java.lang.Object" -> "kotlin.Any"
        in "java.lang.String" -> "kotlin.String"
        in "int", "Int" -> "kotlin.Int"
        in "int[]" -> "kotlin.IntArray"
        in "long" -> "kotlin.Long"
        in "long[]" -> "kotlin.LongArray"
        in "boolean" -> "kotlin.Boolean"
        in "boolean[]" -> "kotlin.BooleanArray"
        in "float" -> "kotlin.Float"
        in "float[]" -> "kotlin.FloatArray"
        in "double" -> "kotlin.Double"
        in "double[]" -> "kotlin.DoubleArray"
        else -> javaPackage
    }

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
        val bodyElements = roundEnv.getElementsAnnotatedWith(Body::class.java).filterNot { it.enclosingElement.toString().contains("copy") }.toMutableSet()
        val queryElements = roundEnv.getElementsAnnotatedWith(Query::class.java).filterNot { it.enclosingElement.toString().contains("copy") }.toMutableSet()

        //        outElementsAllLog(TAG_REQUEST, requestElements)
        //        outElementsAllLog(TAG_BASE_CONFIG, baseConfigElements) 
        //        outElementsAllLog(TAG_HEADER, headersElements)
        //        outElementsAllLog(TAG_BODY, bodyElements)
        //        outElementsAllLog(TAG_QUERY, queryElements)


        //创建请求集合
        val requestDataList = mutableListOf<RequestData>()
        requestElements.forEach { requestElement ->
            val requestAnt = requestElement.getAnnotation(Request::class.java)
            val baseConfigData = baseConfigElements.filterBaseUrlByRequestClass(requestAnt, requestElement.simpleName.toString())
            val headerElements = headersElements.filterHeaderAntByRequestClass(requestElement)
            val headers = headerElements.map {
                HeaderData(it.getAnnotation(Header::class.java).value, it.simpleName.toString())
            }
            val requestClass = requestElement.simpleName.toString()
            val requestType = requestAnt.type
            val requestUrl = getRequestUrl(requestAnt, baseConfigData)

            //获取body相关参数
            val requestBodyData = getRequestBodyData(bodyElements, queryElements, requestElement)
            sendNoteMsg("getRequestBodyData:${requestElement.simpleName},$requestBodyData")

            //获取方法参数
            val methodParameters = getMethodParameters(requestElement, bodyElements, requestBodyData)

            //获取get请求参数
            val requestParameters: List<String> = getRequestParameters(methodParameters, requestAnt, headers, requestBodyData)
            val requestData = RequestData(requestClass, requestType, requestUrl, baseConfigData, headers, methodParameters, requestParameters, requestBodyData)
            requestDataList.add(requestData)
            sendNoteMsg("【RequestData】:$requestData")
        }
        return requestDataList
    }


    /**
     * 获取请求Body的相关信息：参数名、以及Body数据类型，以及相关数据类型的参数结构
     * @param bodyElements MutableSet<out Element>
     * @param queryElements MutableSet<out Element>
     * @param requestElement Element
     * @return RequestBodyData
     */
    private fun getRequestBodyData(bodyElements: MutableSet<out Element>, queryElements: MutableSet<out Element>, requestElement: Element): RequestBodyData { //获取当前请求的@Body元素
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
     * @param bodyType BodyType：X_WWW_FormUrlEncoded、JSON
     * @param currentBodyElement Element?
     * @return RequestBodyData
     */
    private fun createRequestBodyData(bodyType: BodyType, currentBodyElement: Element, queryElements: MutableSet<out Element>): RequestBodyData {
        val currentBodyParameterName = getCurrentBodyParameterName(currentBodyElement)
        return if (bodyType == BodyType.X_WWW_FormUrlEncoded) { //创建XWF数据类型所需的参数@Query列表
            val currentBodyQueryMap = getCurrentBodyQueryMap(currentBodyElement, queryElements)
            if (currentBodyQueryMap.isEmpty()) sendErrorMsg("The request class is ${currentBodyElement.asType()}, the specified BodyType is X_WWW_FormUrlEncoded, but the Query annotation is not declared, resulting in an error")
            RequestBodyData(bodyType, xwfParameters = currentBodyParameterName?.run { Pair(this, currentBodyQueryMap) })
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
     * @param requestBodyData: String 根据BodyType过滤的对应注解的参数（@Body、@X_WWW_FormUrlEncoded）
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
     * 获取请求Url
     * @param requestAnt Request
     * @param baseConfigData BaseConfigData?
     * @return String
     */
    private fun getRequestUrl(requestAnt: Request, baseConfigData: BaseConfigData?): String {
        val url = requestAnt.urlString.asRest("{", "}") //处理全局URL
        return baseConfigData?.run { "\${${packagePath.asBaseUrlClassName(propertyName)}}".plus(url) } ?: url
    }


    /**
     * 获取方法参数列表，根据Class和Parameter注解
     * @param requestElement Element
     * @return List<ParameterData>
     */
    private fun getMethodParameters(requestElement: Element, bodyElements: MutableSet<out Element>, requestBodyData: RequestBodyData?): List<ParameterData> {
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
        }/*when (requestType) {
            RequestType.GET, RequestType.DELETE -> parameters.addAll(getAllParameterByRequestClass(requestElement))
            RequestType.PATCH, RequestType.POST -> {
                requestBodyData?.bodyType?.let { bodyType ->
                    when (bodyType) {
                        BodyType.NONE -> {}
                        BodyType.JSON, BodyType.X_WWW_FormUrlEncoded -> {
                            parameters.addAll(getMethodParameterByBodyKind(bodyElements, requestElement))
                        }
                        else -> {}
                    }
                }
            }
        }*/
        return parameters
    }


    /**
     * 获取方法所有参数，根据Post、Patch请求时的Body参数
     * @param bodyElements MutableSet<out Element>
     * @param requestElement Element
     * @return List<ParameterData>
     */
    private fun getMethodParameterByBodyKind(bodyElements: MutableSet<out Element>, requestElement: Element): List<ParameterData> {
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
     * 过滤URL为Rest风格的参数
     * @receiver List<String>
     * @param restUrl String
     * @return List<String>
     */
    private fun List<String>.filterRestParameters(restUrl: String): List<String> = restUrl.findRest(this)

    /**
     * 查找当前请求的基础URL
     * @receiver MutableSet<out Element>
     * @param request Request
     */
    private fun MutableSet<out Element>.filterBaseUrlByRequestClass(request: Request, requestClass: String): BaseConfigData? {
        val baseConfigElements = this.filter { it.getAnnotation(BaseConfig::class.java).key == request.baseUrlKey }
        return if (baseConfigElements.isNotEmpty()) {
            val baseConfigAnt = baseConfigElements[0].getAnnotation(BaseConfig::class.java)
            if (baseConfigElements.size > 1) {
                sendErrorMsg("point to ${baseConfigElements[1].simpleName}. The @BaseConfig key must be unique, please specify the key for the parameter baseUrlKey in the @Request annotation")
            }
            BaseConfigData(baseConfigAnt.key, baseConfigAnt.enableLog, baseConfigAnt.proxyIp, baseConfigAnt.proxyPort, baseConfigElements[0].simpleName.toString(), baseConfigElements[0].enclosingElement.toString())
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

    private fun sendNoteMsg(msg: String) {
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

    companion object {
        const val TAG_REQUEST = "@Request"
        const val TAG_HEADER = "@Header"
        const val TAG_BODY = "@Body"
        const val TAG_QUERY = "@Query"
        const val TAG_BASE_CONFIG = "@BaseConfig"
        const val KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"
        const val APPLICATION_PKG = "android.app"
        const val APPLICATION = "Application"
        const val ANDROID_VIEW_MODEL_PKG = "com.hearthappy.ktorexpand.viewmodel"
        const val ANDROID_VIEW_MODEL = "BaseAndroidViewModel"
        const val LIVEDATA_PKG = "androidx.lifecycle"
        const val MUTABLE_LIVEDATA = "MutableLiveData"
        const val LIVEDATA = "LiveData"
        const val KTOR_NETWORK_PKG = "com.hearthappy.ktorexpand.code.network"
        const val KTOR_CLIENT_REQUEST_PKG = "io.ktor.client.request"
        const val KTOR_CLIENT_RESPONSE_PKG = "io.ktor.client.statement"
        const val KTOR_REQUEST_SCOPE = "requestScope"
        const val KTOR_REQUEST_STATE = "RequestState"
        const val KTOR_REQUEST = "sendKtorRequest"
        const val GET = "GET"
        const val POST = "POST"
        const val PATCH = "PATCH"
        const val DELETE = "DELETE"
        const val NONE = "NONE"
        const val TEXT = "TEXT"
        const val JSON = "JSON"
        const val HTML = "HTML"
        const val XML = "XML"
        const val FORM_DATA = "FORM_DATA"
        const val X_WWW_FormUrlEncoded = "X_WWW_FormUrlEncoded"
        const val KTOR_PARAMETER = "parameter"
        const val KTOR_HEADER = "header"
        const val LIVEDATA_RESULT = "Result"
        const val HTTP_RESPONSE = "HttpResponse"
        const val STATE_FLOW_PKG = "kotlinx.coroutines.flow"
        const val MUTABLE_STATE_FLOW = "MutableStateFlow"
        const val STATE_FLOW = "StateFlow"
    }
}


