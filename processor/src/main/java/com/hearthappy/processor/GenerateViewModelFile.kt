package com.hearthappy.processor

import com.hearthappy.processor.common.*
import com.hearthappy.processor.model.ServiceConfigData
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

// TODO:优化所需导入的响应类，根据响应类型的包名进行遍历导包
internal fun ViewModelProcessor.generateFileAndWrite(viewModelClassName: String, classBuilder: TypeSpec.Builder, generatedSource: String, serviceConfigList: List<ServiceConfigData>, collectRequiredImport: List<String>) { //创建文件

    val requiredImport = getFinallyRequiredImport(collectRequiredImport)
    //创建文件,导包并取别名import xxx.requestScopeX as RequestScope
    val file = FileSpec.builder(GENERATE_VIEWMODEL_PKG, viewModelClassName).apply {

        //                .addAliasedImport(requestScopeX, "RequestScope") //导包取别名
        //                .addTypeAlias(typeAlias).build() //文件内添加类型别名

        if (requiredImport.isNotEmpty()) {
            addImport(KTOR_NETWORK_PKG, requiredImport)
            addImport(KTOR_CLIENT_REQUEST_PKG, KTOR_PARAMETER, KTOR_HEADER)
            addImport(KTOR_CLIENT_RESPONSE_PKG, HTTP_RESPONSE)
            addImport(KTOR_HTTP_PKG, KTOR_HTTP_RESPONSE, KTOR_CONTENT_TYPE)
        }

        if (serviceConfigList.isNotEmpty() && requiredImport.isNotEmpty()) {
            val map = serviceConfigList.map { it.key }
            addImport(GENERATE_CONFIG_PKG, map)
        }
    }.addType(classBuilder.build()).build()
    file.writeTo(File(generatedSource))
    sendNoteMsg("==================> Create a $viewModelClassName file and write the class to the file")
}

/**
 * 过滤去重，并添加请求域包
 * @param collectRequiredImport List<String>
 * @return List<String>
 */
private fun getFinallyRequiredImport(collectRequiredImport: List<String>): List<String> {
    return collectRequiredImport.isNotEmpty().takeIf { it }?.run { collectRequiredImport.plus(KTOR_REQUEST_SCOPE).plus(KTOR_REQUEST).toSet().toList() } ?: run { collectRequiredImport }
}




