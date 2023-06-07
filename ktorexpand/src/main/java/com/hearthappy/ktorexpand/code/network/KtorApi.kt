package com.hearthappy.ktorexpand.code.network

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.content.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.util.*
import kotlin.random.Random

internal fun generateBoundary(): String = buildString {
    repeat(32) {
        append(Random.nextInt().toString(16))
    }
}.take(70)

internal fun FormBuilder.addAppendToFormData(partData: PartData) {
    append(partData.key, partData.file.readBytes(), headers = if (partData.headers == Headers.Empty) Headers.build {
        fileHeader(partData.contentType, partData.contentDisposition?.run { this }?:"filename=${partData.file.name}")
    } else partData.headers)
}

/**
 * 默认Content-Type:application/json
 * @receiver HttpRequestBuilder
 */
private fun HttpRequestBuilder.jsonHeader() = header(HttpHeaders.ContentType, ContentType.Application.Json)

val textHeader = Header(HttpHeaders.ContentType, ContentType.Text.Plain)

fun HeadersBuilder.fileHeader(contentType: ContentType, contentDisposition: String) {
    append(HttpHeaders.ContentType, contentType)
    append(HttpHeaders.ContentDisposition, contentDisposition)
}

/**
 * 处理headers
 * @receiver HttpRequestBuilder
 * @param headers List<Header>
 */
fun HttpRequestBuilder.handleHeaders(headers: List<Header>?) {
    val contentType = headers?.find { it.key == HttpHeaders.ContentType } //设置默认Content-Type为Json
    contentType ?: jsonHeader()
    headers?.apply { for (header in this) header(header.key, header.value) }
}

fun HttpRequestBuilder.handlerCookies(cookies:List<Cookie>?){
    cookies?.apply { for (cookie in this) cookie(cookie.name, cookie.value) }
}

suspend inline fun HttpClient.getRequest(url: String, headers: List<Header>?,cookies: List<Cookie>?=null, httpRequestScope: HttpRequestBuilder.() -> Unit): HttpResponse = get(urlString = url) {
    handlerCookies(cookies)
    handleHeaders(headers)
    httpRequestScope()
}

suspend inline fun HttpClient.postRequest(url: String, headers: List<Header>?, cookies: List<Cookie>?=null,httpRequestScope: HttpRequestBuilder.() -> Unit): HttpResponse = post(urlString = url) {
    handlerCookies(cookies)
    handleHeaders(headers)
    httpRequestScope()
}

suspend inline fun HttpClient.patchRequest(url: String, headers: List<Header>?,cookies: List<Cookie>?=null, httpRequestScope: HttpRequestBuilder.() -> Unit): HttpResponse = patch(urlString = url) {
    handlerCookies(cookies)
    handleHeaders(headers)
    httpRequestScope()
}

suspend inline fun HttpClient.deleteRequest(url: String, headers: List<Header>?,cookies: List<Cookie>?=null, httpRequestScope: HttpRequestBuilder.() -> Unit): HttpResponse = delete(urlString = url) {
    handlerCookies(cookies)
    handleHeaders(headers)
    httpRequestScope()
}

@OptIn(InternalAPI::class)
suspend fun HttpClient.multiPartRequest(httpType: Int = POST, url: String, headers: List<Header>?,cookies: List<Cookie>?=null, listener: ProgressListener, multipartBody: MultipartBody): HttpResponse {
    return when (httpType) {
        POST -> postRequest(url, headers,cookies) {
            setBody(MultiPartFormDataContent(
                    formData {
                        multipartBody.appends?.apply {
                            for (ap in this) append(ap.key, ap.value, ap.headers)
                        }
                        multipartBody.partData?.apply { addAppendToFormData(this) }
                        multipartBody.multiPartData?.apply { for (partData in this) addAppendToFormData(partData) }
                    },
                    boundary = multipartBody.boundary,
            ))
            onUpload(listener)
        }
        else -> throw RuntimeException("sendKtorUpload not implemented yet，The current RequestType value is $httpType")
    }
}

/**
 * 下载请求
 * @param httpType Int
 * @param url String
 * @param headers List<Header>?
 * @param listener SuspendFunction2<[@kotlin.ParameterName] Long, [@kotlin.ParameterName] Long, Unit>?
 * @param defaultConfig DefaultConfig
 * @return HttpResponse
 */
suspend fun sendKtorDownload(httpType: Int = GET, url: String, headers: List<Header>? = null,cookies: List<Cookie>?=null, listener: ProgressListener = { _, _ -> }, defaultConfig: DefaultConfig = DefaultConfig(EmptyString)) = ktorClient(defaultConfig).use {
    when (httpType) {
        GET  -> it.getRequest(url, headers,cookies) { onDownload(listener) }
        POST -> it.postRequest(url, headers,cookies) { onDownload(listener) }
        else -> throw RuntimeException("sendKtorDownload not implemented yet，The current RequestType value is $httpType")
    }
}

suspend fun sendKtorUpload(httpType: Int = POST, url: String, headers: List<Header>? = null,cookies: List<Cookie>?=null, multipartBody: MultipartBody, listener: ProgressListener = { _, _ -> }, defaultConfig: DefaultConfig = DefaultConfig(EmptyString)) = ktorClient(defaultConfig).use {
    it.multiPartRequest(httpType, url, headers,cookies, listener, multipartBody)
}


/**
 *
 * @param httpType Int
 * @param bodyType Int
 * @param url String
 * @param headers  @Header存在时有数据
 * @param parameters  @Body = NONE时， parameters有数据。通过Params发送数据时
 * @param requestBody @Body = JSON时有数据，通过body发送数据
 * @param appends @Body = FormUrlEncoded 时有数据
 * @return Any?
 */
suspend inline fun sendKtorRequest(httpType: Int = GET, bodyType: Int = NONE, url: String, headers: List<Header>? = null,cookies: List<Cookie>?=null, parameters: HttpRequestBuilder.() -> Unit = {}, requestBody: Any = EmptyContent, appends: ParametersBuilder.() -> Unit = {}, defaultConfig: DefaultConfig = DefaultConfig(EmptyString)) = ktorClient(defaultConfig).use {
    when (httpType) {
        GET    -> {
            when (bodyType) {
                NONE           -> it.getRequest(url, headers,cookies) { parameters() }
                TEXT           -> it.getRequest(url, headers?.plus(textHeader) ?: listOf(textHeader),cookies) { setBody(Gson().toJson(requestBody)) }
                JSON           -> it.getRequest(url, headers,cookies) { setBody(requestBody) }
                FormData       -> it.submitForm(url = url, Parameters.build(appends), encodeInQuery = true) {
                    handlerCookies(cookies)
                    handleHeaders(headers) }
                FormUrlEncoded -> it.getRequest(url, headers,cookies) { setBody(FormDataContent(Parameters.build(appends))) }
                else           -> throw RuntimeException("get other error")
            }
        }
        POST   -> {
            when (bodyType) {
                NONE           -> it.postRequest(url, headers,cookies) { parameters() }
                TEXT           -> it.postRequest(url, headers?.plus(textHeader) ?: listOf(textHeader),cookies) { setBody(Gson().toJson(requestBody)) }
                JSON           -> it.postRequest(url, headers,cookies) { setBody(requestBody) }
                FormData       -> it.submitForm(url = url, Parameters.build(appends)) {
                    handlerCookies(cookies)
                    handleHeaders(headers)
                }
                FormUrlEncoded -> it.postRequest(url, headers,cookies) {
                    setBody(FormDataContent(Parameters.build(appends)))
                }
                else           -> throw RuntimeException("post other error")
            }
        }
        PATCH  -> {
            when (bodyType) {
                NONE           -> it.patchRequest(url, headers,cookies) { parameters() }
                TEXT           -> it.patchRequest(url, headers?.plus(textHeader) ?: listOf(textHeader),cookies) {
                    setBody(Gson().toJson(requestBody))
                }
                JSON           -> it.patchRequest(url, headers,cookies) {
                    setBody(requestBody)
                }
                FormData       -> it.submitForm(url = url, Parameters.build(appends)) {
                    handlerCookies(cookies)
                    handleHeaders(headers)
                }
                FormUrlEncoded -> it.patchRequest(url, headers,cookies) {
                    setBody(FormDataContent(Parameters.build(appends)))
                }
                else           -> throw RuntimeException("patch other error")
            }
        }
        DELETE -> {
            when (bodyType) {
                NONE           -> it.deleteRequest(url, headers,cookies) { parameters() }
                TEXT           -> it.deleteRequest(url, headers?.plus(textHeader) ?: listOf(textHeader),cookies) {
                    setBody(Gson().toJson(requestBody))
                }
                JSON           -> it.deleteRequest(url, headers,cookies) { setBody(requestBody) }
                FormData       -> it.submitForm(url = url, Parameters.build(appends)) {
                    handlerCookies(cookies)
                    handleHeaders(headers)
                }
                FormUrlEncoded -> it.deleteRequest(url, headers,cookies) {
                    setBody(FormDataContent(Parameters.build(appends)))
                }
                else           -> throw RuntimeException("delete other error")
            }
        }
        else   -> {
            throw RuntimeException("sendKtorRequest not implemented yet，The current RequestType value is $httpType")
        }
    }

}


//Request类型
const val GET = 1
const val POST = 2
const val PATCH = 3
const val DELETE = 4


//Body类型
const val NONE = 101
const val TEXT = 102
const val JSON = 103

//const val HTML = 104
//const val XML = 105
const val FormData = 106
const val FormUrlEncoded = 107

//const val MultipartFormData = 108
//const val Streaming = 109

const val EmptyString = ""
const val InSitu = 0


