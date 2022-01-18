package com.hearthappy.ktorexpand.code.network

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() = runBlocking {
    launch {
        val formSubmit =
            formSubmit<HttpResponse>("http://192.168.51.212:50006/c-api/user-login-pwd", appends = {
                append("username", "wxx_1")
                append("password", "24cff18577e8dc8c6fdf53a6621a0b4d")
            }) {}


        println("status:${formSubmit.status}")
    }
    println("end.....")
}

fun contentTypeFromJson(httpRequestBuilder: HttpRequestBuilder) {
    httpRequestBuilder.header(HttpHeaders.ContentType, ContentType.Application.Json)
    httpRequestBuilder.header(HttpHeaders.Accept, "*/*")
    httpRequestBuilder.header(
        HttpHeaders.AcceptEncoding, listOf(ContentType.Application.GZip.contentType)
    )
}


/**
 *
 * @param url String
 * @param httpRequestScope use parameter(key,value)
 * @param headers use header(key,value)
 * @return Response
 */
suspend inline fun <reified Response> getRequest(
    url: String, httpRequestScope: HttpRequestBuilder.() -> Unit
) = ktorClient().use {
    it.get<Response>(url) {
        contentTypeFromJson(this)
        httpRequestScope()
    }
}


/**
 *
 * @param url String
 * @param requestBody use Any
 * @param httpRequestScope use header(key,value)
 * @return Response
 */
suspend inline fun <reified Response> postRequest(
    url: String, requestBody: Any, httpRequestScope: HttpRequestBuilder.() -> Unit
) = ktorClient().use {
    it.post<Response>(url) {
        contentTypeFromJson(this)
        httpRequestScope()
        body = requestBody
    }
}

/**
 *
 * @param url String
 * @param appends use append(key,value)
 * @return Response
 */
suspend inline fun <reified Response> formSubmit(
    url: String,
    appends: ParametersBuilder.() -> Unit,
    httpRequestScope: HttpRequestBuilder.() -> Unit
) = ktorClient().use {

    it.submitForm<Response>(url = url, formParameters = Parameters.build {
        appends()
    }) {
        contentTypeFromJson(this)
        httpRequestScope()
    }
}

suspend inline fun <reified Response> patchRequest(
    url: String, requestBody: Any = EmptyContent, httpRequestScope: HttpRequestBuilder.() -> Unit
) = ktorClient().use {
    it.patch<Response>(urlString = url) {
        contentTypeFromJson(this)
        httpRequestScope()
        if (requestBody != EmptyContent) {
            body = requestBody
        }
    }
}

suspend inline fun <reified Response> deleteRequest(
    url: String, httpRequestScope: HttpRequestBuilder.() -> Unit
) = ktorClient().use {
    it.delete<Response>(urlString = url) {
        contentTypeFromJson(this)
        httpRequestScope()
    }
}






