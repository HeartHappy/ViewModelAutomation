package com.hearthappy.ktorexpand.code.network

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream


suspend fun testFileDownload(fileName: String, file: File) {
    requestScope<File>(io = {
        sendKtorDownload(url = "http://192.168.51.60:9998/fs/$fileName")
    }, onSucceed = { body, _ ->
        println("body:${body.absolutePath}")
    }, onFailure = {}, onThrowable = {}, outFile = file, dispatcher = Dispatchers.IO)
}

suspend inline fun login() = ktorClient().use {
    it.get("https://ktor.io/") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        parameter("price", "asc")
    }
}

/**
 * 测试文件下载示例
 * @param fileOutputStream FileOutputStream
 * @return Long
 */
suspend fun fileDownload(fileOutputStream: FileOutputStream) = ktorClient().use {
    val get = it.get("http://192.168.51.60:9998/fs/1.jpg") {
        onDownload { bytesSentTotal, contentLength ->
            println("bytesSentTotal:$bytesSentTotal,contentLength:$contentLength")
        }
    }
    get.bodyAsChannel().toInputStream().copyTo(fileOutputStream)
}

/**
 * 测试文件上传，返回json
 */
//suspend fun fileUpload() = ktorClient().use {
//    val post = it.post("http://192.168.51.60:9998/upload-json")
//}



fun main() = runBlocking {
    /*val outFile = File("/Library/MyComputer/Software/Android/Git/ViewModelAutomation/test.jpg")

    //fileDownload(outputStream)

    launch {
        testFileDownload("1.jpg", outFile)
    }
*/

    println("end...:")
}