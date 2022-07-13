package com.hearthappy.processor

import com.google.auto.service.AutoService
import com.hearthappy.annotations.*
import com.hearthappy.processor.common.*
import com.hearthappy.processor.log.errorMessage
import com.hearthappy.processor.log.noteMessage
import com.hearthappy.processor.model.*
import com.hearthappy.processor.tools.asRest
import com.hearthappy.processor.tools.findRest
import com.hearthappy.processor.tools.removeWith
import com.hearthappy.processor.tools.substringMiddle
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
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
 *
 * 1、支持设置超时
 *
 * annotation:
 * sendNoteMsg(element.enclosedElements.toList().toString())
 * sendNoteMsg(element.enclosingElement.toString()) //获取包名：com.hearthappy.viewmodelautomation.model
 * sendNoteMsg(element.kind.name) //获取类型：CLASS
 * sendNoteMsg(element.simpleName.toString()) //获取类名
 * sendNoteMsg(element.asType().toString()) //获取类的全相对路径：com.hearthappy.viewmodelautomation.model.ReLogin
 */
@AutoService(Processor::class) class ViewModelProcessor: AbstractProcessor() { //导包所需


    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(AndroidViewModel::class.java.name, BindLiveData::class.java.name, BindStateFlow::class.java.name, Request::class.java.name, Body::class.java.name, Service::class.java.name, ServiceConfig::class.java.name)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?,
    ): Boolean {
        return roundEnv?.processingOver()?.takeIf { it }?.apply { generatedFinish() } ?: processAnnotations(roundEnv)
    }

    private fun processAnnotations(
        roundEnv: RoundEnvironment?,
    ): Boolean {
        return roundEnv?.run {
            val generatedSource = processingEnv.options[KAPT_KOTLIN_GENERATED] ?: run {
                return sendErrorMsg("Can't find target source.")
            }
            val androidViewModelElements = getElementsAnnotatedWith(AndroidViewModel::class.java) //            return handlerAndroidViewModelAnt(androidViewModelElements, this)
            val serviceElements = getElementsAnnotatedWith(Service::class.java)
            val serviceConfigList = getServiceConfigList(serviceElements)
            generateServiceConfig(serviceConfigList, generatedSource)
            generateAndroidViewModel(this, androidViewModelElements, generatedSource, serviceConfigList)
        } ?: run {
            sendErrorMsg("RoundEnvironment is null hence skip the process.")
        }
    }


    private fun generateAndroidViewModel(roundEnv: RoundEnvironment, androidViewModelElements: MutableSet<out Element>, generatedSource: String, serviceConfigList: List<ServiceConfigData>): Boolean {
        if (androidViewModelElements.isEmpty()) {
            return sendErrorMsg("The AndroidViewModel annotation was not found. Please declare AndroidViewModel annotations for Activity or Fragment")
        } //直接获取并封装所有请求

        val requestDataList = getRequestDataList(roundEnv, serviceConfigList)

        androidViewModelElements.forEach { avmElement ->
            val androidViewModel = avmElement.getAnnotation(AndroidViewModel::class.java)
            val bindLiveData = avmElement.getAnnotationsByType(BindLiveData::class.java)
            val bindStateFlow = avmElement.getAnnotationsByType(BindStateFlow::class.java)
            val viewModelClassName = androidViewModel.viewModelClassName.ifEmpty {
                extractName(avmElement.simpleName.toString()).plus("ViewModel")
            }

            //当前ViewModel所需导入的包名
            val collectRequiredImport = mutableListOf<String>() //            val requiredImport = getRequiredImport(requestDataList)

            sendNoteMsg("@AndroidViewModel className:${viewModelClassName},@BindLiveData count:${bindLiveData.size},@BindStateFlow count:${bindStateFlow.size}") //创建类
            val classBuilder = builderViewModelClassSpec(viewModelClassName)


            //通过BindLiveData创建属性、方法
            generatePropertyAndMethodByLiveData(classBuilder, requestDataList, bindLiveData, viewModelClassName) { bld, requestDataList, viewModelParam ->
                sendNoteMsg("==================> Create LiveData: ${bld.methodName} function") //通过类型别名创建函数参数
                generateFunctionByLiveData(bld, requestDataList, viewModelParam, classBuilder, collectRequiredImport)
            }

            //通过BindStateFlow创建属性、方法
            generatePropertyAndMethodByStateFlow(classBuilder, requestDataList, bindStateFlow, viewModelClassName) { bsf, requestDataList, viewModelParam ->
                sendNoteMsg("==================> Create StateFlow: ${bsf.methodName} function") //创建公开属性
                generateFunctionByStateFlow(bsf, requestDataList, viewModelParam, classBuilder, collectRequiredImport)
            }

            //写入文件
            generateFileAndWrite(viewModelClassName, classBuilder, generatedSource, serviceConfigList, collectRequiredImport)
        }
        return true
    }




    private fun builderViewModelClassSpec(viewModelClassName: String): TypeSpec.Builder {
        val classBuilder = TypeSpec.classBuilder(viewModelClassName)
        classBuilder.primaryConstructor(FunSpec.constructorBuilder().addParameter(ParameterSpec("app", application)).build()).addProperty(PropertySpec.builder("app", application).initializer("app").build()).addSuperclassConstructorParameter("app").superclass(androidViewModel)
        return classBuilder
    }





    private fun generatedFinish(): Boolean {
        println("==================> build complete")
        return true
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




    internal fun sendNoteMsg(msg: String) {
        processingEnv.noteMessage { msg }
    }

    internal fun sendErrorMsg(msg: String): Boolean {
        processingEnv.errorMessage { msg }
        return false
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
            TAG_BASE_CONFIG -> ServiceConfig::class.java
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


