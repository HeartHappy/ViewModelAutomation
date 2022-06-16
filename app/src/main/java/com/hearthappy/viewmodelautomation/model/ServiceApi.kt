package com.hearthappy.viewmodelautomation.model

import com.hearthappy.ktorexpand.code.network.ktorClient
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    launch {
        login()
    }
    println("end...")
}

suspend inline fun <reified T> login() = ktorClient(proxyIp = "", proxyPort = -1).use {
    it.get<T>("https://ktor.io/") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        parameter("price", "asc")
    }
}


