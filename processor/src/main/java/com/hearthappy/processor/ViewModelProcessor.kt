package com.hearthappy.processor

import com.google.auto.service.AutoService
import com.hearthappy.annotations.*
import com.hearthappy.processor.common.*
import com.hearthappy.processor.log.errorMessage
import com.hearthappy.processor.log.noteMessage
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.system.measureTimeMillis

/**
 * @Author ChenRui
 * @Date   2021/12/27-16:46
 * @Email  1096885636@qq.com
 * ClassDescription : 注解处理类
 * kotlin poet:
 * liveData.parameterizedBy(responseBean):liveData：声明类型，responseBean：泛型类型
 * FunSpec.addTypeVariable(TypeVariableName("T")) : 函数泛型 ：fun <T> add()
 * FunSpec.addComment("AA"):为函数尾部添加注释  fun(){}  //AA
 * FunSpec.addKdoc("BB"):为函数顶部添加文本注释 /**BB*/ fun(){}
 */
@AutoService(Processor::class)
class ViewModelProcessor : AbstractProcessor() { //导包所需

    private var startingTime = 0L
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(AndroidViewModel::class.java.name, Service::class.java.name)

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?,
    ): Boolean = roundEnv?.processingOver()?.takeIf { it }?.run { generatedFinish() } ?: processAnnotations(roundEnv)

    private fun processAnnotations(
        roundEnv: RoundEnvironment?,
    ): Boolean {
        return roundEnv?.run {
            val generatedSource = processingEnv.options[KAPT_KOTLIN_GENERATED] ?: run {
                return sendErrorMsg("Can't find target source.")
            }
            startingTime = System.currentTimeMillis()

            val timeMillis = measureTimeMillis {
                val androidViewModelElements = getElementsAnnotatedWith(AndroidViewModel::class.java) //            return handlerAndroidViewModelAnt(androidViewModelElements, this)
                val serviceElements = getElementsAnnotatedWith(Service::class.java)
                val serviceConfigList = getServiceConfigList(serviceElements)
                generateServiceConfigFile(serviceConfigList, generatedSource)
                generateAndroidViewModelFile(this, androidViewModelElements, generatedSource, serviceConfigList)
            }
            sendNoteMsg("==================> The build is complete, it takes ${timeMillis}ms")
            true
        } ?: sendErrorMsg("RoundEnvironment is null hence skip the process.")
    }


    private fun generatedFinish(): Boolean {
//        println("==================> build complete.Takes ${System.currentTimeMillis() - startingTime}ms")
        return false
    }


    internal fun sendNoteMsg(msg: String) = processingEnv.noteMessage { msg }

    internal fun sendErrorMsg(msg: String): Boolean {
        processingEnv.errorMessage { msg }
        return false
    }


    @Suppress("unused") fun outElementsAllLog(tag: Any, elements: MutableSet<out Element>) {
        for (element in elements) outElementLog(tag, element)
    }

    fun outElementLog(tag: Any, element: Element?) {
        element ?: return
        val value = when (tag) {
            TAG_REQUEST     -> Request::class.java
            TAG_HEADER      -> Header::class.java
            TAG_BODY        -> Body::class.java
            TAG_QUERY       -> Query::class.java
            TAG_BASE_CONFIG -> ServiceConfig::class.java
            TAG_ORDER       -> Order::class.java
            else            -> Request::class.java
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


