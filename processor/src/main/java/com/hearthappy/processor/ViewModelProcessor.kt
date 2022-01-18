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
 *
 * 4、支持patch、delete
 * 5、支持rest请求
 * 6、全局基础URL，在使用中支持更改
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
    private val androidViewModel = ClassName(ANDROID_VIEWMODEL_PKG, ANDROID_VIEWMODEL)

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(AndroidViewModel::class.java.name, BindLiveData::class.java.name, BindStateFlow::class.java.name, Request::class.java.name, BaseUrl::class.java.name)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?,
    ): Boolean {

        return if (roundEnv?.processingOver() == true) {
            generatedFinish()
        } else {
            processAnnotations(annotations, roundEnv)
        }
    }

    private fun processAnnotations(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?,
    ): Boolean {

        annotations ?: run {
            sendErrorMsg("TypeElement is null or empty hence skip the process.")
            return false
        }

        return roundEnv?.run {
            val androidViewModelElements = this.getElementsAnnotatedWith(AndroidViewModel::class.java) //            return handlerAndroidViewModelAnt(androidViewModelElements, this)

            val requestDataList = createRequestData(this)
            handlerAndroidViewModel(androidViewModelElements, requestDataList)
        } ?: run {
            sendErrorMsg("RoundEnvironment is null hence skip the process.")
            false
        }
    }

    private fun handlerAndroidViewModel(androidViewModelElements: MutableSet<out Element>, requestDataList: List<RequestData>): Boolean {
        if (androidViewModelElements.isEmpty()) {
            sendErrorMsg("The AndroidViewModel annotation was not found. Please declare AndroidViewModel annotations for Activity or Fragment")
            return false
        }
        val generatedSource = processingEnv.options[KAPT_KOTLIN_GENERATED] ?: run {
            sendErrorMsg("Can't find target source.")
            return false
        }
        androidViewModelElements.forEach { AVMElement ->
            val androidViewModel = AVMElement.getAnnotation(AndroidViewModel::class.java)
            val bindLiveData = AVMElement.getAnnotationsByType(BindLiveData::class.java)
            val bindStateFlow = AVMElement.getAnnotationsByType(BindStateFlow::class.java)
            val viewModelClassName = androidViewModel.viewModelClassName.ifEmpty {
                extractName(AVMElement.simpleName.toString()).plus("ViewModel")
            }
            sendNoteMsg("@AndroidViewModel className:${viewModelClassName},${bindLiveData.size},${bindStateFlow.size}")

            //创建类
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
                val viewModelParam = getViewModelParam(it)
                sendNoteMsg("==================> Create a private StateFlow") //                classBuilder.addProperty(PropertySpec.builder(viewModelParam.priPropertyName,mutableStateFlow.parameterizedBy(requestState)).delegate("lazy{}").build())
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
                val viewModelParam = getViewModelParam(it)

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
        val function = FunSpec.builder(it.methodName).apply { //                    generateIO(roundEnv, requestClass, responseBody)
            generateMethodParametersSpec(requestDataList, viewModelParam)
            generateMethodRequestScope(requestDataList, viewModelParam)
            addStatement("onFailure = { ${viewModelParam.priPropertyName}.postValue(Result.Error(it))},")
            addStatement("onSucceed = { ${viewModelParam.priPropertyName}.postValue(Result.Success(it))})")
        }
        classBuilder.addFunction(function.build())
    }


    private fun generateFunctionByStateFlow(it: BindStateFlow, requestDataList: List<RequestData>, viewModelParam: GenerateViewModelData, classBuilder: TypeSpec.Builder) {
        val function = FunSpec.builder(it.methodName).apply {
            generateMethodParametersSpec(requestDataList, viewModelParam)
            addStatement("${viewModelParam.priPropertyName}.value = $KTOR_REQUEST_STATE.LOADING")
            generateMethodRequestScope(requestDataList, viewModelParam)
            addStatement("onFailure = { ${viewModelParam.priPropertyName}.value = $KTOR_REQUEST_STATE.FAILED(it) },")
            addStatement("onSucceed = { ${viewModelParam.priPropertyName}.value = $KTOR_REQUEST_STATE.SUCCEED(it) })")
        }
        classBuilder.addFunction(function.build())
    }

    private fun FunSpec.Builder.generateMethodRequestScope(requestDataList: List<RequestData>, viewModelParam: GenerateViewModelData) {
        if (requestDataList.isEmpty()) {
            addStatement("requestScope(io = io,")
        } else {
            val findRequestData = requestDataList.find { it.requestClass == viewModelParam.requestBody.simpleName }
            addStatement("requestScope(io = {")
            findRequestData?.apply {

                when (requestType) {
                    RequestType.GET -> {
                        addStatement("getRequest<${viewModelParam.responseBody}>(url=\"$url\", httpRequestScope = {")
                        headers.forEach { header -> addStatement("header(\"${header.key}\",${header.parameterName})") }
                        requestParameters.forEach { parameter -> addStatement("parameter(\"${parameter}\", ${parameter})") }
                    }
                    RequestType.POST -> { //两集合找差集
                        addStatement("postRequest<${viewModelParam.responseBody}>(url = \"$url\",") //添加请求头代码
                        requestBodyParameter?.also { addStatement("requestBody=$it,") }
                        addStatement("httpRequestScope={")
                        headers.forEach { header -> addStatement("header(\"${header.key}\",${header.parameterName})") }
                    }
                    RequestType.FormUrlEncoded -> {
                        addStatement("formSubmit<${viewModelParam.responseBody}>(url = \"$url\", appends = {")
                        requestParameters.forEach { parameter -> addStatement("append(\"${parameter}\", ${parameter})") }
                        addStatement("},httpRequestScope={")
                        headers.forEach { header -> addStatement("header(\"${header.key}\",${header.parameterName})") }
                    }
                    RequestType.PATCH -> {
                        addStatement("patchRequest<${viewModelParam.responseBody}>(url=\"$url\",")
                        requestBodyParameter?.also { addStatement("requestBody=$it,") }
                        addStatement("httpRequestScope={")
                        headers.forEach { header -> addStatement("header(\"${header.key}\",${header.parameterName})") }
                    }
                    RequestType.DELETE -> {
                        addStatement("deleteRequest<${viewModelParam.responseBody}>(url=\"$url\", httpRequestScope = {")
                        headers.forEach { header -> addStatement("header(\"${header.key}\",${header.parameterName})") }
                        requestParameters.forEach { parameter -> addStatement("parameter(\"${parameter}\", ${parameter})") }
                    }
                }

                addStatement("})")
            }
            addStatement("},")
        }
    }


    /**
     * 通过BindLiveData获取生成ViewModel所需参数
     * @param ant Annotation:BindLiveData、BindStateFlow
     * @return GenerateViewModelData
     */
    private fun getViewModelParam(ant: Annotation): GenerateViewModelData {
        var requestClass = ""
        var responseClass = ""
        var pubPropertyName = ""
        when (ant) {
            is BindLiveData -> {
                requestClass = ant.getAnnotationValue { it.requestClass }.toString()
                responseClass = ant.getAnnotationValue { bld -> bld.responseClass }.toString()
                pubPropertyName = ant.liveDataName.ifEmpty { ant.methodName.plus(LIVEDATA) }
            }
            is BindStateFlow -> {
                requestClass = ant.getAnnotationValue { it.requestClass }.toString()
                responseClass = ant.getAnnotationValue { bld -> bld.responseClass }.toString()
                pubPropertyName = ant.stateFlowName.ifEmpty { ant.methodName.plus(STATE_FLOW) }
            }
            else -> Unit
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
        } else {

            //有@Request注解时，自动生成响应请求
            val findRequestData = requestDataList.find { it.requestClass == viewModelParam.requestBody.simpleName }
            findRequestData?.methodParameters?.forEach {
                addParameter(it.parameterName, it.parameterType.asKotlinClassName())
            }
        }
    }

    private fun builderViewModelClassSpec(viewModelClassName: String): TypeSpec.Builder {
        val classBuilder = TypeSpec.classBuilder(viewModelClassName)
        classBuilder.primaryConstructor(FunSpec.constructorBuilder().addParameter("app", application).build()).addSuperclassConstructorParameter("app").superclass(androidViewModel)
        return classBuilder
    }


    private fun generateFileAndWrite(viewModelClassName: String, classBuilder: TypeSpec.Builder, generatedSource: String) { //创建文件
        //创建文件,导包并取别名import xxx.requestScopeX as RequestScope
        sendNoteMsg("==================> Create a file and write the class to the file")
        val packageName = "com.hearthappy.compiler"
        val file = FileSpec.builder(packageName, viewModelClassName)

            //                .addAliasedImport(requestScopeX, "RequestScope") //导包取别名
            //                .addTypeAlias(typeAlias).build() //文件内添加类型别名
            .addImport(KTOR_NETWORK_PKG, KTOR_REQUEST_SCOPE, KTOR_REQUEST_GET, KTOR_REQUEST_POST, KTOR_REQUEST_FORM_SUBMIT, KTOR_REQUEST_PATCH, KTOR_REQUEST_DELETE).addImport(KTOR_CLIENT_REQUEST_PKG, KTOR_PARAMETER, KTOR_HEADER).addType(classBuilder.build()).build()


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
            if (paramName == "<init>" || paramName.contains("get") || paramName.first().isUpperCase() /*|| paramName==findHeaderAnt?.simpleName.toString()*/) continue

            if (ele.simpleName.toString() == "copy") {
                val substringMiddle = ele.toString().substringMiddle("(", ")", 1, 1).also { sendNoteMsg(it) }
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
        println("==================> generated finish.")
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

    private fun extractName(className: String): String {
        return when {
            className.contains("Activity") -> className.substringBefore("Activity")
            className.contains("Fragment") -> className.substringBefore("Fragment")
            else -> className
        }
    }


    /**
     * Create RequestData
     * @param roundEnv RoundEnvironment
     */
    private fun createRequestData(roundEnv: RoundEnvironment): List<RequestData> { //获取所有注解，将请求集中在一起
        val requestElements = roundEnv.getElementsAnnotatedWith(Request::class.java)
        val headersElements = roundEnv.getElementsAnnotatedWith(Header::class.java)
        val bodyElements = roundEnv.getElementsAnnotatedWith(Body::class.java)
        val baseUrlElements = roundEnv.getElementsAnnotatedWith(BaseUrl::class.java)

        outElementAllLog(TAG_REQUEST, requestElements)
        outElementAllLog(TAG_HEADER, headersElements)
        outElementAllLog(TAG_BODY, bodyElements)
        outElementAllLog(TAG_BASE_URL, baseUrlElements)


        //创建请求集合
        val requestDataList = mutableListOf<RequestData>()
        requestElements.forEach { requestElement ->
            val requestAnt = requestElement.getAnnotation(Request::class.java)
            val headerElements = headersElements.filterAntParamByRequestClass(requestElement)
            val baseUrlData = baseUrlElements.filterBaseUrlByRequestClass(requestAnt)
            val requestClass = requestElement.simpleName.toString()
            val requestType = requestAnt.type
            val requestUrl = getRequestUrl(requestAnt, baseUrlData)
            val headers = headerElements.map { HeaderData(it.getAnnotation(Header::class.java).value, it.simpleName.toString()) }
            val methodParameters = getMethodParameters(bodyElements, requestElement, requestType)
            val requestBodyParameter = getRequestBodyParameter(bodyElements, requestElement, requestAnt.type)
            val requestParameters = getRequestParameters(methodParameters, headers, requestAnt.urlString, requestBodyParameter)

            val requestData = RequestData(requestClass, requestType, requestUrl, baseUrlData, headers, methodParameters, requestParameters, requestBodyParameter)
            requestDataList.add(requestData)
            sendNoteMsg("【RequestData】:$requestData")
        }
        return requestDataList
    }


    /**
     * 获取body参数
     * @param bodyElements MutableSet<out Element>
     * @param requestElement Element
     * @return String
     */
    private fun getRequestBodyParameter(bodyElements: MutableSet<out Element>, requestElement: Element, type: RequestType): String? {
        val bodyElement = bodyElements.find { if (it.kind == ElementKind.CLASS) it.simpleName == requestElement.simpleName else it.enclosingElement.toString().removeWith("(", ")") == requestElement.simpleName.toString() }
        return bodyElement?.run {
            if (type == RequestType.PATCH || type == RequestType.POST) {
                when (this.kind) {
                    ElementKind.PARAMETER -> {
                        this.simpleName.toString()
                    }
                    ElementKind.CLASS -> {
                        this.simpleName.toString().replaceFirstChar { it.lowercase(Locale.getDefault()) }
                    }
                    else -> null
                }
            } else null
        }
    }

    /**
     * 获取请求时的参数
     * @param parameters List<ParameterData>
     * @param headers List<HeaderData>
     * @param url String
     * @return List<String>
     */
    private fun getRequestParameters(parameters: List<ParameterData>, headers: List<HeaderData>, url: String, requestBodyParameter: String?): List<String> {

        //过滤headers参数
        val filterHeaderParameters = (parameters.map { it.parameterName } subtract headers.map { it.parameterName }).toList()

        //过滤rest参数
        val filterRestParameters = filterHeaderParameters.filterRestParameters(url)

        //过滤body参数
        return filterRestParameters.filter { it != requestBodyParameter }
    }

    /**
     * 获取请求Url
     * @param requestAnt Request
     * @param baseUrlData BaseUrlData?
     * @return String
     */
    private fun getRequestUrl(requestAnt: Request, baseUrlData: BaseUrlData?): String {
        val url = requestAnt.urlString.asRest("{", "}") //处理全局URL
        return baseUrlData?.run { "\${${packagePath.asBaseUrlClassName(propertyName)}}".plus(url) } ?: url
    }

    /**
     * 获取方法参数列表，根据Class和Parameter注解
     * @param bodyElements MutableSet<out Element>
     * @param requestElement Element
     * @return List<ParameterData>
     */
    private fun getMethodParameters(bodyElements: MutableSet<out Element>, requestElement: Element, requestType: RequestType): List<ParameterData> {
        val parameters = mutableListOf<ParameterData>()
        when (requestType) {
            RequestType.GET, RequestType.FormUrlEncoded, RequestType.DELETE -> parameters.addAll(getAllParameterByRequestClass(requestElement))
            RequestType.POST, RequestType.PATCH -> parameters.addAll(getMethodParameterByBodyKind(bodyElements, requestElement))
        }
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
            sendErrorMsg("${requestElement.simpleName} For a Post request, the Body could not be found, the @Body annotation can be declared on a class or parameter")
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
    private fun List<String>.filterRestParameters(restUrl: String): List<String> = restUrl.findRest(this).also { sendNoteMsg("findRest:$it") }

    /**
     * 查找当前请求的基础URL
     * @receiver MutableSet<out Element>
     * @param request Request
     */
    private fun MutableSet<out Element>.filterBaseUrlByRequestClass(request: Request): BaseUrlData? {
        val baseUrlElements = this.filter { it.getAnnotation(BaseUrl::class.java).key == request.baseUrlKey }
        return if (baseUrlElements.isNotEmpty()) {
            val baseUrlAnt = baseUrlElements[0].getAnnotation(BaseUrl::class.java)
            if (baseUrlElements.size > 1) {
                sendErrorMsg("The @BaseUrl key must be unique, please specify the key for the parameter BaseUrlKey in the @Request annotation")
            }
            BaseUrlData(baseUrlAnt.key, baseUrlElements[0].simpleName.toString(), baseUrlElements[0].enclosingElement.toString())
        } else {
            null
        }
    }

    /**
     * 过滤注解参数
     * @receiver MutableSet<out Element>
     * @param requestElement Element 根据请求Class
     * @return List<Element>
     */
    private fun MutableSet<out Element>.filterAntParamByRequestClass(requestElement: Element): List<Element> {
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
        val bodyForParamAnt = this.filterAntParamByRequestClass(requestElement)
        if (bodyForParamAnt.isNotEmpty()) return bodyForParamAnt

        //如果没找到，则查找为Class注解
        return this.filter { it.asType() == requestElement.asType() }
    }

    private fun sendNoteMsg(msg: String) {
        processingEnv.noteMessage { msg }
    }

    private fun sendErrorMsg(msg: String) {
        processingEnv.errorMessage { msg }
    }

    private fun outElementAllLog(tag: Any, elements: MutableSet<out Element>) {
        sendNoteMsg("======================<Gorgeous dividing line>=======================")
        elements.forEach { element ->
            sendNoteMsg("$tag simpleName:${element.simpleName}")
            sendNoteMsg("$tag enclosedElements:${element.enclosedElements.toList()}")
            sendNoteMsg("$tag enclosingElement:${element.enclosingElement}")
            sendNoteMsg("$tag kind:${element.kind}")
            val value = when (tag) {
                TAG_REQUEST -> Request::class.java
                TAG_HEADER -> Header::class.java
                TAG_BODY -> Body::class.java
                TAG_BASE_URL -> BaseUrl::class.java
                else -> Request::class.java
            }
            sendNoteMsg("$tag getAnnotation:${element.getAnnotation(value)}")
            sendNoteMsg("$tag asType:${element.asType()}")
            sendNoteMsg("$tag element:${element}")
            sendNoteMsg("---------------------------------------------------")
        }
    }

    companion object {
        const val TAG_REQUEST = "@Request"
        const val TAG_HEADER = "@Header"
        const val TAG_BODY = "@Body"
        const val TAG_BASE_URL = "@BaseUrl"
        const val KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"
        const val APPLICATION_PKG = "android.app"
        const val APPLICATION = "Application"
        const val ANDROID_VIEWMODEL_PKG = "com.hearthappy.ktorexpand.viewmodel"
        const val ANDROID_VIEWMODEL = "BaseAndroidViewModel"
        const val LIVEDATA_PKG = "androidx.lifecycle"
        const val MUTABLE_LIVEDATA = "MutableLiveData"
        const val LIVEDATA = "LiveData"
        const val KTOR_NETWORK_PKG = "com.hearthappy.ktorexpand.code.network"
        const val KTOR_CLIENT_REQUEST_PKG = "io.ktor.client.request"
        const val KTOR_REQUEST_SCOPE = "requestScope"
        const val KTOR_REQUEST_STATE = "RequestState"
        const val KTOR_REQUEST_GET = "getRequest"
        const val KTOR_REQUEST_POST = "postRequest"
        const val KTOR_REQUEST_FORM_SUBMIT = "formSubmit"
        const val KTOR_REQUEST_PATCH = "patchRequest"
        const val KTOR_REQUEST_DELETE = "deleteRequest"
        const val KTOR_PARAMETER = "parameter"
        const val KTOR_HEADER = "header"
        const val LIVEDATA_RESULT = "Result"
        const val STATE_FLOW_PKG = "kotlinx.coroutines.flow"
        const val MUTABLE_STATE_FLOW = "MutableStateFlow"
        const val STATE_FLOW = "StateFlow"
    }
}


