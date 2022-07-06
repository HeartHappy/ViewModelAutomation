package com.hearthappy.ktorexpand.code.network

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.cio.*
import io.ktor.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() {

    runBlocking {
        launch {
            val sendRequest = sendKtorRequest<Response>(POST,
                X_WWW_FormUrlEncoded,
                "https://api.it120.cc/HeartHappy/user/email/login",
                appends = {
                    append("deviceId", "JKS")
                    append("deviceName", "XIAOMI_K40")
                    append("email", "1096885636@qq.com")
                    append("pwd", "123456")
                })
            println("status:${sendRequest}")
            println("result:$sendRequest")
        }
        println("end.....")
    }
}

fun HttpRequestBuilder.jsonHeader() {
    header(HttpHeaders.ContentType, ContentType.Application.Json)
}

suspend inline fun <reified Response> HttpClient.getRequest(
    url: String,
    headers: HttpRequestBuilder.() -> Unit = { jsonHeader() },
    httpRequestScope: HttpRequestBuilder.() -> Unit
): Response = get(urlString = url) {
    headers()
    httpRequestScope()
}

suspend inline fun <reified Response> HttpClient.postRequest(
    url: String,
    headers: HttpRequestBuilder.() -> Unit = { jsonHeader() },
    httpRequestScope: HttpRequestBuilder.() -> Unit
): Response = post(urlString = url) {
    headers()
    httpRequestScope()
}

suspend inline fun <reified Response> HttpClient.patchRequest(
    url: String,
    headers: HttpRequestBuilder.() -> Unit = { jsonHeader() },
    httpRequestScope: HttpRequestBuilder.() -> Unit
): Response = patch(urlString = url) {
    headers()
    httpRequestScope()
}

suspend inline fun <reified Response> HttpClient.deleteRequest(
    url: String,
    headers: HttpRequestBuilder.() -> Unit = { jsonHeader() },
    httpRequestScope: HttpRequestBuilder.() -> Unit
): Response = delete(urlString = url) {
    headers()
    httpRequestScope()
}


/**
 *
 * @param requestType Int
 * @param bodyType Int
 * @param url String
 * @param headers  @Header存在时有数据
 * @param parameters  @Body = NONE时， parameters有数据。通过Params发送数据时
 * @param requestBody @Body = JSON时有数据，通过body发送数据
 * @param appends @Body = X_WWW_FormUrlEncoded 时有数据
 * @return Any?
 */
suspend inline fun <reified Response> sendKtorRequest(
    requestType: Int,
    bodyType: Int,
    url: String,
    headers: HttpRequestBuilder.() -> Unit = {},
    parameters: HttpRequestBuilder.() -> Unit = {},
    requestBody: Any = EmptyContent,
    appends: ParametersBuilder.() -> Unit = {},
    enableLog: Boolean = true,
    proxyIp: String = EmptyString,
    proxyPort: Int = -1
) = ktorClient(enableLog, proxyIp, proxyPort).use {
    when (requestType) {
        GET -> {
            when (bodyType) {
                NONE -> it.getRequest(url, headers) { parameters() }
                TEXT -> it.getRequest(url, headers) { body = GsonSerializer().write(requestBody) }
                JSON -> it.getRequest(url, headers) { body = requestBody }
                FORM_DATA -> it.submitForm(
                    url = url, Parameters.build(appends), encodeInQuery = true
                ) { headers() }
                X_WWW_FormUrlEncoded -> it.getRequest(url, headers) {
                    body = FormDataContent(Parameters.build(appends))
                }
                else -> throw RuntimeException("get other error")
            }
        }
        POST -> {
            when (bodyType) {
                NONE -> it.postRequest(url, headers) { parameters() }
                TEXT -> it.postRequest(url, headers) { body = GsonSerializer().write(requestBody) }
                JSON -> it.postRequest(url, headers) { body = requestBody }
                FORM_DATA -> it.submitForm(url = url, Parameters.build(appends)) { headers() }
                X_WWW_FormUrlEncoded -> it.postRequest(url, headers) {
                    body = FormDataContent(Parameters.build(appends))
                }
                else -> throw RuntimeException("post other error")
            }
        }
        PATCH -> {
            when (bodyType) {
                NONE -> it.patchRequest(url, headers) { parameters() }
                TEXT -> it.patchRequest(url, headers) { body = GsonSerializer().write(requestBody) }
                JSON -> it.patchRequest(url, headers) { body = requestBody }
                FORM_DATA -> it.submitForm(url = url, Parameters.build(appends)) { headers() }
                X_WWW_FormUrlEncoded -> it.patchRequest(url, headers) {
                    body = FormDataContent(Parameters.build(appends))
                }
                else -> throw RuntimeException("patch other error")
            }
        }
        DELETE -> {
            when (bodyType) {
                NONE -> it.deleteRequest(url, headers) { parameters() }
                TEXT -> it.deleteRequest(url, headers) {
                    body = GsonSerializer().write(requestBody)
                }
                JSON -> it.deleteRequest(url, headers) { body = requestBody }
                FORM_DATA -> it.submitForm(url = url, Parameters.build(appends)) { headers() }
                X_WWW_FormUrlEncoded -> it.deleteRequest<Response>(url, headers) {
                    body = FormDataContent(Parameters.build(appends))
                }
                else -> throw RuntimeException("delete other error")
            }
        }
        else -> {
            throw RuntimeException("KtorApi not implemented yet，The current RequestType value is $requestType")
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
const val FORM_DATA = 106
const val X_WWW_FormUrlEncoded = 107
const val EmptyString = ""


