package com.hearthappy.processor.common

import com.squareup.kotlinpoet.ClassName

const val TAG_REQUEST = "@Request"
const val TAG_HEADER = "@Header"
const val TAG_BODY = "@Body"
const val TAG_QUERY = "@Query"
const val TAG_BASE_CONFIG = "@BaseConfig"

const val KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"
const val APPLICATION_PKG = "android.app"
const val APPLICATION = "Application"
const val ANDROIDX_LIFECYCLE_PKG = "androidx.lifecycle"
const val ANDROID_VIEW_MODEL = "AndroidViewModel"
const val MUTABLE_LIVEDATA = "MutableLiveData"
const val LIVEDATA = "LiveData"

const val STATE_FLOW_PKG = "kotlinx.coroutines.flow"
const val MUTABLE_STATE_FLOW = "MutableStateFlow"
const val STATE_FLOW = "StateFlow"

const val KTOR_NETWORK_PKG = "com.hearthappy.ktorexpand.code.network"
const val GENERATE_CONFIG_PKG="com.hearthappy.compiler.config"
const val GENERATE_VIEWMODEL_PKG="com.hearthappy.compiler.viewmodel"

const val KTOR_REQUEST_SCOPE = "requestScope"
const val KTOR_REQUEST_STATE = "RequestState"
const val KTOR_DEFAULT_CONFIG = "DefaultConfig"
const val KTOR_REQUEST = "sendKtorRequest"
const val KTOR_CONTENT_TYPE = "ContentType"

const val KTOR_CLIENT_REQUEST_PKG = "io.ktor.client.request"
const val KTOR_CLIENT_RESPONSE_PKG = "io.ktor.client.statement"
const val KTOR_HTTP_PKG = "io.ktor.http"
const val KTOR_HTTP_RESPONSE = "HttpHeaders"






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
const val FormUrlEncoded = "FormUrlEncoded"
const val KTOR_PARAMETER = "parameter"
const val KTOR_HEADER = "header"
const val LIVEDATA_RESULT = "Result"
const val HTTP_RESPONSE = "HttpResponse"


internal val application = ClassName(APPLICATION_PKG, APPLICATION)
internal val androidViewModel = ClassName(ANDROIDX_LIFECYCLE_PKG, ANDROID_VIEW_MODEL)
internal val mutableStateFlow =
    ClassName(STATE_FLOW_PKG, MUTABLE_STATE_FLOW)
internal val stateFlow = ClassName(STATE_FLOW_PKG, STATE_FLOW)
internal val requestState =
    ClassName(KTOR_NETWORK_PKG, KTOR_REQUEST_STATE)

internal val mutableLiveData =
    ClassName(ANDROIDX_LIFECYCLE_PKG, MUTABLE_LIVEDATA)
internal val liveData = ClassName(ANDROIDX_LIFECYCLE_PKG, LIVEDATA)
internal val result =
    ClassName(KTOR_NETWORK_PKG, LIVEDATA_RESULT)



