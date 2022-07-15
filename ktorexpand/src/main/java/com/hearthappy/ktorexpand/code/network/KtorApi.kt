package com.hearthappy.ktorexpand.code.network

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope

data class ReLogin(val username: String, val password: String)


suspend fun main() {
    runCatching {
        coroutineScope {
            val submitForm =
                ktorClient().submitForm("http://192.168.51.23:50000/c-api/user-login-pwd",
                    Parameters.build {
                        append("username", "user_3")
                        append("password", "24cff18577e8dc8c6fdf53a6621a0b4d")
                    })
            println("result:$submitForm")
        }
    }
}

fun HttpRequestBuilder.jsonHeader() {
    header(HttpHeaders.ContentType, ContentType.Application.Json)
}

suspend inline fun HttpClient.getRequest(
    url: String,
    headers: HttpRequestBuilder.() -> Unit,
    httpRequestScope: HttpRequestBuilder.() -> Unit
): HttpResponse = get(urlString = url) {
    headers()
    httpRequestScope()
}

suspend inline fun HttpClient.postRequest(
    url: String,
    headers: HttpRequestBuilder.() -> Unit,
    httpRequestScope: HttpRequestBuilder.() -> Unit
): HttpResponse = post(urlString = url) {
    headers()
    httpRequestScope()
}

suspend inline fun HttpClient.patchRequest(
    url: String,
    headers: HttpRequestBuilder.() -> Unit,
    httpRequestScope: HttpRequestBuilder.() -> Unit
): HttpResponse = patch(urlString = url) {
    headers()
    httpRequestScope()
}

suspend inline fun HttpClient.deleteRequest(
    url: String,
    headers: HttpRequestBuilder.() -> Unit,
    httpRequestScope: HttpRequestBuilder.() -> Unit
): HttpResponse = delete(urlString = url) {
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
 * @param appends @Body = FormUrlEncoded 时有数据
 * @return Any?
 */
suspend inline fun sendKtorRequest(
    requestType: Int = GET,
    bodyType: Int = NONE,
    url: String,
    crossinline headers: HttpRequestBuilder.() -> Unit = {},
    parameters: HttpRequestBuilder.() -> Unit = {},
    requestBody: Any = EmptyContent,
    appends: ParametersBuilder.() -> Unit = {},
    defaultConfig: DefaultConfig = DefaultConfig(
        EmptyString
    )
) = ktorClient(defaultConfig).use {
    when (requestType) {
        GET -> {
            when (bodyType) {
                NONE -> it.getRequest(url, headers) { parameters() }
                TEXT -> it.getRequest(
                    url, headers
                ) { setBody(jacksonObjectMapper().writeValueAsString(requestBody)) }
                JSON -> it.getRequest(url, headers) { setBody(requestBody) }
                FORM_DATA -> it.submitForm(
                    url = url, Parameters.build(appends), encodeInQuery = true
                ) { headers() }
                FormUrlEncoded -> it.getRequest(url, headers) {
                    setBody(FormDataContent(Parameters.build(appends)))
                }
                else -> throw RuntimeException("get other error")
            }
        }
        POST -> {
            when (bodyType) {
                NONE -> it.postRequest(url, headers) { parameters() }
                TEXT -> it.postRequest(
                    url,
                    headers
                ) { setBody(jacksonObjectMapper().writeValueAsString(requestBody)) }
                JSON -> it.postRequest(url, headers) { setBody(requestBody) }
                FORM_DATA -> it.submitForm(url = url, Parameters.build(appends)) { headers() }
                FormUrlEncoded -> it.postRequest(url, headers) {
                    setBody(FormDataContent(Parameters.build(appends)))
                }
                else -> throw RuntimeException("post other error")
            }
        }
        PATCH -> {
            when (bodyType) {
                NONE -> it.patchRequest(url, headers) { parameters() }
                TEXT -> it.patchRequest(
                    url,
                    headers
                ) { setBody(jacksonObjectMapper().writeValueAsString(requestBody)) }
                JSON -> it.patchRequest(url, headers) { setBody(requestBody) }
                FORM_DATA -> it.submitForm(url = url, Parameters.build(appends)) { headers() }
                FormUrlEncoded -> it.patchRequest(url, headers) {
                    setBody(FormDataContent(Parameters.build(appends)))
                }
                else -> throw RuntimeException("patch other error")
            }
        }
        DELETE -> {
            when (bodyType) {
                NONE -> it.deleteRequest(url, headers) { parameters() }
                TEXT -> it.deleteRequest(url, headers) {
                    setBody(jacksonObjectMapper().writeValueAsString(requestBody))
                }
                JSON -> it.deleteRequest(url, headers) { setBody(requestBody) }
                FORM_DATA -> it.submitForm(url = url, Parameters.build(appends)) { headers() }
                FormUrlEncoded -> it.deleteRequest(url, headers) {
                    setBody(FormDataContent(Parameters.build(appends)))
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
const val FormUrlEncoded = 107
const val EmptyString = ""


