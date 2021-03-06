package com.hearthappy.ktorexpand.code.network

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

suspend inline fun login() = ktorClient().use {
    it.get("https://ktor.io/") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        parameter("price", "asc")
    }
}

fun main() = runBlocking {
    val measureTimeMillis = measureTimeMillis {
        for (i in 1..10) {/*launch {
                val login = login()
                println("requestTime:${login.version}")
            }*/
            launch {
                requestScope<String>(io = { login() }, onSucceed = { _, http ->
                    println("requestTime:${http.requestTime}")
                }, onFailure = {}, onThrowable = {}, Dispatchers.Default)
            }
            delay(200)
        }
    }

    println("end...:$measureTimeMillis")
}


/**
 * 默认Content-Type:application/json
 * @receiver HttpRequestBuilder
 */
fun HttpRequestBuilder.jsonHeader() = header(HttpHeaders.ContentType, ContentType.Application.Json)

val textHeader = Header(HttpHeaders.ContentType, ContentType.Text.Plain)

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

suspend inline fun HttpClient.getRequest(url: String, headers: List<Header>?, httpRequestScope: HttpRequestBuilder.() -> Unit): HttpResponse = get(urlString = url) {
    handleHeaders(headers)
    httpRequestScope()
}

suspend inline fun HttpClient.postRequest(url: String, headers: List<Header>?, httpRequestScope: HttpRequestBuilder.() -> Unit): HttpResponse = post(urlString = url) {
    handleHeaders(headers)
    httpRequestScope()
}

suspend inline fun HttpClient.patchRequest(url: String, headers: List<Header>?, httpRequestScope: HttpRequestBuilder.() -> Unit): HttpResponse = patch(urlString = url) {
    handleHeaders(headers)
    httpRequestScope()
}

suspend inline fun HttpClient.deleteRequest(url: String, headers: List<Header>?, httpRequestScope: HttpRequestBuilder.() -> Unit): HttpResponse = delete(urlString = url) {
    handleHeaders(headers)
    httpRequestScope()
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
suspend inline fun sendKtorRequest(httpType: Int = GET, bodyType: Int = NONE, url: String, headers: List<Header>? = null, parameters: HttpRequestBuilder.() -> Unit = {}, requestBody: Any = EmptyContent, appends: ParametersBuilder.() -> Unit = {}, defaultConfig: DefaultConfig = DefaultConfig(EmptyString)) = ktorClient(defaultConfig).use {
    when (httpType) {
        GET -> {
            when (bodyType) {
                NONE -> it.getRequest(url, headers) { parameters() }
                TEXT -> it.getRequest(url, headers?.plus(textHeader) ?: listOf(textHeader)) { setBody(Gson().toJson(requestBody)) }
                JSON -> it.getRequest(url, headers) { setBody(requestBody) }
                FormData -> it.submitForm(url = url, Parameters.build(appends), encodeInQuery = true) { handleHeaders(headers) }
                FormUrlEncoded -> it.getRequest(url, headers) { setBody(FormDataContent(Parameters.build(appends))) }
                else -> throw RuntimeException("get other error")
            }
        }
        POST -> {
            when (bodyType) {
                NONE -> it.postRequest(url, headers) { parameters() }
                TEXT -> it.postRequest(url, headers?.plus(textHeader) ?: listOf(textHeader)) { setBody(Gson().toJson(requestBody)) }
                JSON -> it.postRequest(url, headers) { setBody(requestBody) }
                FormData -> it.submitForm(url = url, Parameters.build(appends)) {
                    handleHeaders(headers)
                }
                FormUrlEncoded -> it.postRequest(url, headers) { setBody(FormDataContent(Parameters.build(appends))) }
                else -> throw RuntimeException("post other error")
            }
        }
        PATCH -> {
            when (bodyType) {
                NONE -> it.patchRequest(url, headers) { parameters() }
                TEXT -> it.patchRequest(url, headers?.plus(textHeader) ?: listOf(textHeader)) {
                    setBody(Gson().toJson(requestBody))
                }
                JSON -> it.patchRequest(url, headers) {
                    setBody(requestBody)
                }
                FormData -> it.submitForm(url = url, Parameters.build(appends)) {
                    handleHeaders(headers)
                }
                FormUrlEncoded -> it.patchRequest(url, headers) { setBody(FormDataContent(Parameters.build(appends))) }
                else -> throw RuntimeException("patch other error")
            }
        }
        DELETE -> {
            when (bodyType) {
                NONE -> it.deleteRequest(url, headers) { parameters() }
                TEXT -> it.deleteRequest(url, headers?.plus(textHeader) ?: listOf(textHeader)) { setBody(Gson().toJson(requestBody)) }
                JSON -> it.deleteRequest(url, headers) { setBody(requestBody) }
                FormData -> it.submitForm(url = url, Parameters.build(appends)) {
                    handleHeaders(headers)
                }
                FormUrlEncoded -> it.deleteRequest(url, headers) {
                    setBody(FormDataContent(Parameters.build(appends)))
                }
                else -> throw RuntimeException("delete other error")
            }
        }
        else -> {
            throw RuntimeException("KtorApi not implemented yet，The current RequestType value is $httpType")
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
const val HTML = 104
const val XML = 105
const val FormData = 106
const val FormUrlEncoded = 107
const val MultipartFormData = 108

const val EmptyString = ""
const val InSitu = 0


