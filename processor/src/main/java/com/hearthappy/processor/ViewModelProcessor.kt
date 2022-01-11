package com.hearthappy.processor

import com.google.auto.service.AutoService
import com.hearthappy.annotations.*
import com.hearthappy.processor.log.errorMessage
import com.hearthappy.processor.log.noteMessage
import com.hearthappy.processor.model.GenerateViewModelData
import com.hearthappy.processor.model.HeaderData
import com.hearthappy.processor.model.ParameterData
import com.hearthappy.processor.model.RequestData
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
 * 3、支持patch、delete
 * 4、支持rest请求
 * 5、全局基础URL，在使用中支持更改
 * 6、支持添加请求头
 *
 * annotation:
 * sendNoteMsg(element.enclosedElements.toList().toString())
 * sendNoteMsg(element.enclosingElement.toString()) //获取包名：com.hearthappy.viewmodelautomation.model
 * sendNoteMsg(element.kind.name) //获取类型
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
        return mutableSetOf(AndroidViewModel::class.java.name, BindLiveData::class.java.name, BindStateFlow::class.java.name, Request::class.java.name)
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
                sendNoteMsg("==================> 创建私有StateFlow")
                generateProperty(propertyName = viewModelParam.priPropertyName, propertyType = mutableStateFlow.parameterizedBy(requestState), initValue = "$MUTABLE_STATE_FLOW<$KTOR_REQUEST_STATE>($KTOR_REQUEST_STATE.DEFAULT)", addToClass = classBuilder, KModifier.PRIVATE)

                sendNoteMsg("==================> 创建公有StateFlow") //创建公开属性
                generateProperty(propertyName = viewModelParam.pubPropertyName, propertyType = stateFlow.parameterizedBy(requestState), initValue = viewModelParam.priPropertyName, addToClass = classBuilder)

                sendNoteMsg("==================> 创建StateFlow:${it.methodName}函数") //创建公开属性
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

                sendNoteMsg("==================> 创建私有LiveData") //创建私有属性
                generateProperty(propertyName = viewModelParam.priPropertyName, propertyType = mutableLiveData.parameterizedBy(result.parameterizedBy(viewModelParam.responseBody)), initValue = "$MUTABLE_LIVEDATA<${result.simpleName}<${viewModelParam.responseBody.simpleName}>>()", addToClass = classBuilder, KModifier.PRIVATE)

                sendNoteMsg("==================> 创建公有LiveData") //创建公开属性
                generateProperty(propertyName = viewModelParam.pubPropertyName, propertyType = liveData.parameterizedBy(result.parameterizedBy(viewModelParam.responseBody)), initValue = viewModelParam.priPropertyName, addToClass = classBuilder)

                sendNoteMsg("==================> 创建LiveData:${it.methodName}函数") //通过类型别名创建函数参数
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
                val subtractParameters = parameters.map { it.parameterName } subtract headers.map { it.parameterName }
                when (requestType) {
                    RequestType.GET -> {
                        addStatement("getRequest<${viewModelParam.responseBody}>(url=\"$url\", parameters = {")
                        headers.forEach { header -> addStatement("header(\"${header.key}\",${header.parameterName})") }
                        subtractParameters.forEach { parameter -> addStatement("parameter(\"${parameter}\", ${parameter})") }
                    }
                    RequestType.POST -> { //两集合找差集
                        addStatement("postRequest<${viewModelParam.responseBody}>(url = \"$url\", requestBody = ${subtractParameters.toList()[0]},headers = {") //添加请求头代码
                        headers.forEach { header -> addStatement("header(\"${header.key}\",${header.parameterName})") }
                    }
                    RequestType.FormUrlEncoded -> {
                        addStatement("formSubmit<${viewModelParam.responseBody}>(url = \"$url\", appends = {")
                        subtractParameters.forEach { parameter ->
                            addStatement("append(\"${parameter}\", ${parameter})")
                        }
                        addStatement("},headers={")
                        headers.forEach { header -> addStatement("header(\"${header.key}\",${header.parameterName})") }
                    }
                    RequestType.PATCH -> {}
                    RequestType.DELETE -> {}
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
        } else { //有@Request注解时，自动生成响应请求
            val findRequestData = requestDataList.find { it.requestClass == viewModelParam.requestBody.simpleName }
            findRequestData?.parameters?.forEach {
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
        sendNoteMsg("==================> 创建文件并将类写入文件中")
        val packageName = "com.hearthappy.compile"
        val file = FileSpec.builder(packageName, viewModelClassName)

            //                .addAliasedImport(requestScopeX, "RequestScope") //导包取别名
            //                .addTypeAlias(typeAlias).build() //文件内添加类型别名
            .addImport(KTOR_NETWORK_PKG, KTOR_REQUEST_SCOPE, KTOR_REQUEST_GET, KTOR_REQUEST_POST, KTOR_REQUEST_FORM_SUBMIT).addImport(KTOR_CLIENT_REQUEST_PKG, KTOR_PARAMETER, KTOR_HEADER).addType(classBuilder.build()).build()


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


    private fun generateProperty(
        propertyName: String,
        propertyType: ParameterizedTypeName,
        initValue: String,
        addToClass: TypeSpec.Builder,
        vararg modifier: KModifier,
    ) {
        addToClass.addProperty(PropertySpec.builder(propertyName, propertyType).initializer(initValue).addModifiers(*modifier).build())
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
        outElementAllLog(TAG_REQUEST, requestElements)
        outElementAllLog(TAG_HEADER, headersElements)
        outElementAllLog(TAG_BODY, bodyElements)


        //创建请求集合
        val requestDataList = mutableListOf<RequestData>()
        requestElements.forEach { requestElement ->
            val requestAnt = requestElement.getAnnotation(Request::class.java)
            val headerElements = headersElements.filterAntParamByRequestClass(requestElement)
            val requestClass = requestElement.simpleName.toString()
            val requestType = requestAnt.type
            val url = requestAnt.urlString
            val headers = headerElements.map {
                HeaderData(it.getAnnotation(Header::class.java).value, it.simpleName.toString())
            }
            val parameters = getParameters(bodyElements, requestElement, requestType)

            sendNoteMsg("【RequestData】:requestClass：$requestClass,requestType:$requestType,headers:$headers,parameters:${parameters},url:$url,")
            requestDataList.add(RequestData(requestClass, requestType, url, headers, parameters))
        }
        return requestDataList
    }


    /**
     * 获取参数列表，根据Class和Parameter注解
     * @param bodyElements MutableSet<out Element>
     * @param requestElement Element
     * @return List<ParameterData>
     */
    private fun getParameters(bodyElements: MutableSet<out Element>, requestElement: Element, requestType: RequestType): List<ParameterData> {

        val parameters = mutableListOf<ParameterData>()
        when (requestType) {
            RequestType.GET, RequestType.FormUrlEncoded -> parameters.addAll(getAllParameterByRequestClass(requestElement))
            RequestType.POST -> getBodyRequestParameters(bodyElements, requestElement, parameters)
            RequestType.DELETE -> {}
            RequestType.PATCH -> {}
        }
        return parameters
    }


    /**
     * 获取请求类型为Post时的Body参数
     * @param bodyElements MutableSet<out Element>
     * @param requestElement Element
     * @param parameters MutableList<ParameterData>
     */
    private fun getBodyRequestParameters(bodyElements: MutableSet<out Element>, requestElement: Element, parameters: MutableList<ParameterData>) {
        val filterBodyElements = bodyElements.filterBodyAntByRequestClass(requestElement)
        filterBodyElements.forEach { bodyElement ->
            when (bodyElement.kind) {
                ElementKind.CLASS -> {
                    parameters.add(ParameterData(bodyElement.simpleName.toString().replaceFirstChar { it.lowercase(Locale.getDefault()) }, bodyElement.asType().toString()))
                }
                ElementKind.PARAMETER -> {
                    parameters.addAll(getAllParameterByRequestClass(requestElement))
                }
                else -> Unit
            }
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
    private fun MutableSet<out Element>.filterBodyAntByRequestClass(requestElement: Element): List<Element> { //过滤为Parameter类型的body注解
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
        sendNoteMsg("======================<华丽分割线>=======================")
        elements.forEach { element ->
            sendNoteMsg("$tag simpleName:${element.simpleName}")
            sendNoteMsg("$tag enclosedElements:${element.enclosedElements.toList()}")
            sendNoteMsg("$tag enclosingElement:${element.enclosingElement}")
            sendNoteMsg("$tag kind:${element.kind}")
            val value = when (tag) {
                TAG_REQUEST -> Request::class.java
                TAG_HEADER -> Header::class.java
                TAG_BODY -> Body::class.java
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
        const val KTOR_PARAMETER = "parameter"
        const val KTOR_HEADER = "header"
        const val LIVEDATA_RESULT = "Result"
        const val STATE_FLOW_PKG = "kotlinx.coroutines.flow"
        const val MUTABLE_STATE_FLOW = "MutableStateFlow"
        const val STATE_FLOW = "StateFlow"
    }
}