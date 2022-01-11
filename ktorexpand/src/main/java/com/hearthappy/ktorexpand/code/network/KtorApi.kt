package com.hearthappy.ktorexpand.code.network

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() = runBlocking {
    launch {
        val formSubmit = formSubmit<HttpResponse>("http://192.168.51.212:50006/c-api/user-login-pwd", appends = {
            append("username", "wxx_1")
            append("password", "24cff18577e8dc8c6fdf53a6621a0b4d")
        })
        println("status:${formSubmit.status}")
    }
    println("end.....")
}

fun contentTypeFromJson(httpRequestBuilder: HttpRequestBuilder) {
    httpRequestBuilder.header(HttpHeaders.ContentType, ContentType.Application.Json)
    httpRequestBuilder.header(HttpHeaders.Accept, "*/*")
    httpRequestBuilder.header(HttpHeaders.AcceptEncoding, listOf(ContentType.Application.GZip.contentType))
}


/**
 *
 * @param url String
 * @param parameters use parameter(key,value)
 * @param headers use header(key,value)
 * @return Response
 */
suspend inline fun <reified Response> getRequest(url: String, parameters: HttpRequestBuilder.() -> Unit = ::contentTypeFromJson) = ktorClient().use {
    it.get<Response>(url) {
        contentTypeFromJson(this)
        parameters()
    }
}


/**
 *
 * @param url String
 * @param requestBody use Any
 * @param headers use header(key,value)
 * @return Response
 */
suspend inline fun <reified Response> postRequest(url: String, requestBody: Any, headers: HttpRequestBuilder.() -> Unit = ::contentTypeFromJson) = ktorClient().use {
    it.post<Response>(url) {
        contentTypeFromJson(this)
        headers()
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
        headers: HttpRequestBuilder.() -> Unit = ::contentTypeFromJson
) = ktorClient().use {

    it.submitForm<Response>(url = url, formParameters = Parameters.build {
        appends()
    }) {
        contentTypeFromJson(this)
        headers()
    }
}

suspend inline fun <reified Response> patchRequest(url: String, parameters: HttpRequestBuilder.() -> Unit) = ktorClient().use {
    it.patch<Response>(urlString = url) {
        contentTypeFromJson(this)
        parameters()
    }
}

suspend inline fun <reified Response> deleteRequest(url: String, parameters: HttpRequestBuilder.() -> Unit) = ktorClient().use {
    it.delete<Response>(urlString = url) {
        contentTypeFromJson(this)
        parameters()
    }
}






