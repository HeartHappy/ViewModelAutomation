package com.hearthappy.processor.common

import com.squareup.kotlinpoet.ClassName

const val TAG_REQUEST = "@Request"
const val TAG_HEADER = "@Header"
const val TAG_BODY = "@Body"
const val TAG_QUERY = "@Query"
const val TAG_BASE_CONFIG = "@BaseConfig"
const val TAG_ORDER = "@Order"
const val TAG_COOKIE = "@Cookie"

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

//network package
const val NETWORK_PKG = "com.hearthappy.ktorexpand.code.network"
const val GENERATE_CONFIG_PKG="com.hearthappy.compiler.config"
const val GENERATE_VIEWMODEL_PKG="com.hearthappy.compiler.viewmodel"
const val NETWORK_REQUEST_SCOPE = "requestScope"
const val NETWORK_REQUEST_STATE = "RequestState"
const val NETWORK_LIVEDATA_RESULT = "Result"
const val NETWORK_DEFAULT_CONFIG = "DefaultConfig"
const val NETWORK_REQUEST = "sendKtorRequest"
const val NETWORK_DOWNLOAD = "sendKtorDownload"
const val NETWORK_UPLOAD = "sendKtorUpload"
const val NETWORK_CONTENT_TYPE = "ContentType"
const val NETWORK_HEADER = "Header"
const val NETWORK_Cookie = "Cookie"
const val NETWORK_MultipartBody = "MultipartBody"

//ktor package
const val KTOR_CLIENT_REQUEST_PKG = "io.ktor.client.request"
const val KTOR_CLIENT_RESPONSE_PKG = "io.ktor.client.statement"
const val KTOR_PROGRESS_PKG = "io.ktor.client.content.ProgressListener"
const val KTOR_HTTP_PKG = "io.ktor.http"
const val KTOR_HTTP_RESPONSE = "HttpHeaders"
const val KTOR_PARAMETER = "parameter"





internal val application = ClassName(APPLICATION_PKG, APPLICATION)
internal val androidViewModel = ClassName(ANDROIDX_LIFECYCLE_PKG, ANDROID_VIEW_MODEL)
internal val mutableStateFlow =
    ClassName(STATE_FLOW_PKG, MUTABLE_STATE_FLOW)
internal val stateFlow = ClassName(STATE_FLOW_PKG, STATE_FLOW)
internal val requestState =
    ClassName(NETWORK_PKG, NETWORK_REQUEST_STATE)

internal val mutableLiveData =
    ClassName(ANDROIDX_LIFECYCLE_PKG, MUTABLE_LIVEDATA)
internal val liveData = ClassName(ANDROIDX_LIFECYCLE_PKG, LIVEDATA)
internal val result =
    ClassName(NETWORK_PKG, NETWORK_LIVEDATA_RESULT)



