package com.hearthappy.processor

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

// TODO:优化所需导入的响应类，根据响应类型的包名进行遍历导包
internal fun ViewModelProcessor.generateFileAndWrite(
    viewModelClassName: String, classBuilder: TypeSpec.Builder, generatedSource: String
) { //创建文件
    //创建文件,导包并取别名import xxx.requestScopeX as RequestScope
    sendNoteMsg("==================> Create a file and write the class to the file")
    val packageName = "com.hearthappy.compiler"
    val file = FileSpec.builder(packageName, viewModelClassName)

        //                .addAliasedImport(requestScopeX, "RequestScope") //导包取别名
        //                .addTypeAlias(typeAlias).build() //文件内添加类型别名
        .addImport(
            KTOR_NETWORK_PKG,
            KTOR_REQUEST_SCOPE,
            KTOR_REQUEST,
            GET,
            POST,
            PATCH,
            DELETE,
            NONE,
            TEXT,
            HTML,
            XML,
            JSON,
            FORM_DATA,
            X_WWW_FormUrlEncoded
        ) //            .addImport(KTOR_NETWORK_PKG,"*")
        .addImport(
            KTOR_CLIENT_REQUEST_PKG,
            KTOR_PARAMETER,
            KTOR_HEADER
        ).addImport(
            KTOR_CLIENT_RESPONSE_PKG, HTTP_RESPONSE
        ) //            .addImport(KTOR_CLIENT_RESPONSE_PKG)
        .addImport(
            KTOR_HTTP_PKG,
            KTOR_HTTP_RESPONSE,
            KTOR_CONTENT_TYPE
        ).addType(classBuilder.build()).build()


    file.writeTo(File(generatedSource))
}

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
const val KTOR_HTTP_PKG = "io.ktor.http"
const val KTOR_HTTP_RESPONSE = "HttpHeaders"
const val KTOR_CONTENT_TYPE = "ContentType"

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
