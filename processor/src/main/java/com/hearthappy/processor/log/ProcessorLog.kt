package com.hearthappy.processor.log

import com.hearthappy.annotations.Header
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

/**
 * @Author ChenRui
 * @Date   2021/12/27-16:52
 * @Email  1096885636@qq.com
 * ClassDescription :
 */
fun ProcessingEnvironment.noteMessage(message: () -> String) {
    this.messager.printMessage(Diagnostic.Kind.NOTE, message())
}

fun ProcessingEnvironment.errorMessage(message: () -> String) {
    this.messager.printMessage(Diagnostic.Kind.ERROR, message())
}





