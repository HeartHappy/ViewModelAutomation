package com.hearthappy.processor

import com.google.auto.service.AutoService
import com.hearthappy.annotations.AndroidViewModel
import com.hearthappy.annotations.BindLiveData
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.processor.log.errorMessage
import com.hearthappy.processor.log.noteMessage
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.KClass

/**
 * @Author ChenRui
 * @Date   2021/12/27-16:46
 * @Email  1096885636@qq.com
 * ClassDescription :
 * liveData.parameterizedBy(responseBean):liveData：声明类型，responseBean：泛型类型
 * FunSpec.addTypeVariable(TypeVariableName("T")) : 函数泛型 ：fun <T> add()
 * FunSpec.addComment("AA"):为函数尾部添加注释  fun(){}  //AA
 * FunSpec.addKdoc("BB"):为函数顶部添加文本注释 /**BB*/ fun(){}
 */
@AutoService(Processor::class) class ViewModelProcessor : AbstractProcessor() { //导包所需

    private val application = ClassName(APPLICATION_PKG, APPLICATION)
    private val androidViewModel = ClassName(
        ANDROID_VIEWMODEL_PKG, ANDROID_VIEWMODEL
    )
    private val string = ClassName("kotlin", STRING)
    private val any = ClassName("kotlin", "Any")


    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            AndroidViewModel::class.java.name,
            BindLiveData::class.java.name,
            BindStateFlow::class.java.name
        )
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?,
    ): Boolean {

        return if (roundEnv?.processingOver() == true) {
            generatedPlugins()
        } else {
            processAnnotations(annotations, roundEnv)
        }
    }

    private fun processAnnotations(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?,
    ): Boolean {
        if (roundEnv == null) {
            processingEnv.noteMessage { "RoundEnvironment is null hence skip the process." }
            return false
        }
        if (annotations == null) {
            processingEnv.noteMessage { "TypeElement is null or empty hence skip the process." }
            return false
        }
        val androidViewModelElements =
            roundEnv.getElementsAnnotatedWith(AndroidViewModel::class.java)
        val generatedSource = processingEnv.options[KAPT_KOTLIN_GENERATED] ?: run {
            processingEnv.errorMessage { "Can't find target source." }
            return false
        }
        val packageName = "com.hearthappy.compiler" //添加引用了一个尚不存在的类
        processingEnv.noteMessage { "========================获取AndroidViewModel注解===========================" }



        androidViewModelElements.forEach { element ->

            val viewModelAnnotation = element.getAnnotation(AndroidViewModel::class.java)
            val bindLiveDataList = element.getAnnotationsByType(BindLiveData::class.java)
            val bindStateFlowList = element.getAnnotationsByType(BindStateFlow::class.java)
            val viewModelClassName = viewModelAnnotation.viewModelClassName.ifEmpty {
                extractName(element.simpleName.toString()).plus("ViewModel")
            }
            processingEnv.noteMessage { "ViewModel className:${viewModelClassName}" }

            //创建类
            val classBuilder = TypeSpec.classBuilder(viewModelClassName)

            classBuilder.primaryConstructor(
                FunSpec.constructorBuilder().addParameter(
                    "app", application
                ).build()
            ).addSuperclassConstructorParameter("app").superclass(androidViewModel)

            if (bindLiveDataList.isNotEmpty()) {
                val mutableLiveData = ClassName(LIVEDATA_PKG, MUTABLE_LIVEDATA)
                val liveData = ClassName(LIVEDATA_PKG, LIVEDATA)
                val result = ClassName(KTOR_NETWORK_PKG, LIVEDATA_RESULT)
                bindLiveDataList.forEach {
                    val type = it.getAnnotationClassValue().toString()
                    processingEnv.noteMessage { "type:$type" }
                    val splitPackage = splitPackage(asKotlinPackage(type))
                    val propertyName = it.liveDataName.ifEmpty { it.methodName.plus(LIVEDATA) }
                    val responseBody = ClassName(splitPackage.first, splitPackage.second)

                    processingEnv.noteMessage { "==============创建私有LiveData===========" } //创建私有属性
                    createProperty(
                        propertyName = privateName(propertyName),
                        propertyType = mutableLiveData.parameterizedBy(
                            result.parameterizedBy(responseBody)
                        ),
                        initValue = "$MUTABLE_LIVEDATA<${result.simpleName}<${splitPackage.second}>>()",
                        addToClass = classBuilder,
                        KModifier.PRIVATE
                    )


                    processingEnv.noteMessage { "==============创建公有LiveData===========" } //创建公开属性
                    createProperty(
                        propertyName = propertyName,
                        propertyType = liveData.parameterizedBy(result.parameterizedBy(responseBody)),
                        initValue = privateName(propertyName),
                        addToClass = classBuilder
                    )

                    processingEnv.noteMessage { "===========创建${it.methodName}函数==============" } //通过类型别名创建函数参数
                    val lambdaTypeName = LambdaTypeName.get(returnType = responseBody)
                    val function = FunSpec.builder(it.methodName).apply {
                        addParameter(
                            ParameterSpec.builder("io", lambdaTypeName.copy(suspending = true))
                                .build()
                        )
                        addStatement("requestScope(io = io,")
                        addStatement("onFailure = { ${privateName(propertyName)}.postValue(Result.Error(it))},")
                        addStatement("onSucceed = { ${privateName(propertyName)}.postValue(Result.Success(it))})")
                    }

                    processingEnv.noteMessage { "==============添加函数至当前类===============" }
                    classBuilder.addFunction(function.build())
                }
            }


            processingEnv.noteMessage { "================创建StateFlow=================" }
            if (bindStateFlowList.isNotEmpty()) { //导包StateFlow相关包
                val mutableStateFlow = ClassName(STATE_FLOW_PKG, MUTABLE_STATE_FLOW)
                val stateFlow = ClassName(STATE_FLOW_PKG, STATE_FLOW)
                val requestState = ClassName(KTOR_NETWORK_PKG, KTOR_REQUEST_STATE)

                bindStateFlowList.forEach {
                    val type = element.getAnnotationClassValue<BindStateFlow> { this.responseClass }
                        .toString()
                    processingEnv.noteMessage { "type:$type" }
                    val splitPackage = splitPackage(asKotlinPackage(type))
                    val propertyName = it.stateFlowName.ifEmpty { "${it.methodName}$STATE_FLOW" }
                    processingEnv.noteMessage { "split:${splitPackage.first},${splitPackage.second}" }
                    val responseBody = ClassName(splitPackage.first, splitPackage.second) //创建属性
                    createProperty(
                        propertyName = privateName(propertyName),
                        propertyType = mutableStateFlow.parameterizedBy(requestState),
                        initValue = "$MUTABLE_STATE_FLOW<$KTOR_REQUEST_STATE>($KTOR_REQUEST_STATE.DEFAULT)",
                        addToClass = classBuilder,
                        KModifier.PRIVATE
                    )

                    createProperty(
                        propertyName = propertyName,
                        propertyType = stateFlow.parameterizedBy(requestState),
                        initValue = privateName(propertyName),
                        addToClass = classBuilder
                    )

                    val function = FunSpec.builder(it.methodName).apply {
                        addParameter(
                            "io",
                            LambdaTypeName.get(returnType = responseBody).copy(suspending = true)
                        )
                        addStatement("${privateName(propertyName)}.value = $KTOR_REQUEST_STATE.LOADING")
                        addStatement("requestScope(io = io,")
                        addStatement("onFailure = { ${privateName(propertyName)}.value = $KTOR_REQUEST_STATE.FAILED(it) },")
                        addStatement("onSucceed = { ${privateName(propertyName)}.value = $KTOR_REQUEST_STATE.SUCCEED(it) })")
                    }
                    classBuilder.addFunction(function.build())
                }
            }


            //创建文件
            //创建文件,导包并取别名import xxx.requestScopeX as RequestScope
            processingEnv.noteMessage { "============创建文件并将类写入文件中==============" }
            val file = FileSpec.builder(packageName, viewModelClassName).addImport(
                KTOR_NETWORK_PKG, KTOR_REQUEST_SCOPE
            ) //                .addAliasedImport(requestScopeX, "RequestScope") //导包取别名
                //                .addTypeAlias(typeAlias).build() //文件内添加类型别名
                .addType(classBuilder.build()).build()


            file.writeTo(File(generatedSource))
        }
        return true
    }

    private fun createProperty(
        propertyName: String,
        propertyType: ParameterizedTypeName,
        initValue: String,
        addToClass: TypeSpec.Builder,
        vararg modifier: KModifier,
    ) {
        addToClass.addProperty(
            PropertySpec.builder(propertyName, propertyType).initializer(initValue)
                .addModifiers(*modifier).build()
        )
    }


    private fun generatedPlugins(): Boolean {
        println("-----> generatedPlugins <-----")
        return true
    }


    private fun privateName(name: String) = "_$name"

    private fun splitPackage(fullClassName: String): Pair<String, String> {
        val lastIndexOf = fullClassName.lastIndexOf(".")
        val packageName = fullClassName.subSequence(0, lastIndexOf)
        val className = fullClassName.subSequence(lastIndexOf + 1, fullClassName.length)
        return Pair(packageName.toString(), className.toString())
    }

    /**
     * 获取注解参数为KClass<*>会出现异常时，通过异常获取返回值
     * @receiver BindLiveData
     * @return (Any..Any?)
     */
    private fun BindLiveData.getAnnotationClassValue() = try {
        this.responseClass
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }

    private fun asKotlinPackage(javaPackage: String) = if (javaPackage.contains("java")) {
        when {
            javaPackage.contains("Object") -> "kotlin.Any"
            javaPackage.contains("String") -> "kotlin.String"
            else -> javaPackage
        }
    } else {
        javaPackage
    }

    private fun extractName(className: String): String {
        return when {
            className.contains("Activity") -> className.substringBefore("Activity")
            className.contains("Fragment") -> className.substringBefore("Fragment")
            else -> className
        }
    }

    private inline fun <reified T : Annotation> Element.getAnnotationClassValue(f: T.() -> KClass<*>) =
        try {
            getAnnotation(T::class.java).f()
            throw Exception("Expected to get a MirroredTypeException")
        } catch (e: MirroredTypeException) {
            e.typeMirror
        }

    companion object {
        const val KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"
        const val APPLICATION_PKG = "android.app"
        const val APPLICATION = "Application"
        const val ANDROID_VIEWMODEL_PKG = "com.hearthappy.ktorexpand.viewmodel"
        const val ANDROID_VIEWMODEL = "BaseAndroidViewModel"
        const val LIVEDATA_PKG = "androidx.lifecycle"
        const val MUTABLE_LIVEDATA = "MutableLiveData"
        const val LIVEDATA = "LiveData"
        const val KTOR_NETWORK_PKG = "com.hearthappy.ktorexpand.code.network"
        const val KTOR_REQUEST_SCOPE = "requestScope"
        const val KTOR_REQUEST_STATE = "RequestState"
        const val LIVEDATA_RESULT = "Result"
        const val STATE_FLOW_PKG = "kotlinx.coroutines.flow"
        const val MUTABLE_STATE_FLOW = "MutableStateFlow"
        const val STATE_FLOW = "StateFlow"
        const val STRING = "String"
    }
}