package com.hearthappy.ktorexpand.code.network

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() = runBlocking {
    launch {


        getRequest("https://ktor.io/", parameters = { parameter("price", "asc") })


        /*formSubmit("http://192.168.51.212:50006/c-api/user-login-pwd") {
                append("username", "wxx_1")
                append("password", "24cff18577e8dc8c6fdf53a6621a0b4d")
            }*/
    }
    println("end.....")
}

fun contentTypeFromJson(httpRequestBuilder: HttpRequestBuilder) {
    httpRequestBuilder.header(HttpHeaders.ContentType, ContentType.Application.Json)
}

/**
 *
 * @param url String
 * @param requestBody use Any
 * @param headers use header(key,value)
 * @return Response
 */
suspend inline fun <reified Request, reified Response> postRequest(
    url: String,
    requestBody: Request,
    headers: HttpRequestBuilder.() -> Unit = ::contentTypeFromJson
) = ktorClient().use {
    it.post<Response>(url) {
        headers()
        requestBody?.let { b -> body = b }
    }
}


/**
 *
 * @param url String
 * @param parameters use parameter(key,value)
 * @param headers use header(key,value)
 * @return Response
 */
suspend inline fun <reified Response> getRequest(
    url: String,
    parameters: HttpRequestBuilder.() -> Unit,
    headers: HttpRequestBuilder.() -> Unit = ::contentTypeFromJson
) = ktorClient().use {
    it.get<Response>(url) {
        headers()
        parameters()
    }
}

/**
 *
 * @param url String
 * @param parameters use append(key,value)
 * @return Response
 */
suspend inline fun <reified Response> formSubmit(
    url: String,
    parameters: Parameters,
) = ktorClient().use {
    it.submitForm<Response>(url = url, formParameters = parameters)
}

public fun HttpRequestBuilder.parameter(key: String, value: Any?): Unit =
    value?.let { url.parameters.append(key, it.toString()) } ?: Unit







