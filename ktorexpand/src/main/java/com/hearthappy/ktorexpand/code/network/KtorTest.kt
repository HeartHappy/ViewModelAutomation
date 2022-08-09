package com.hearthappy.ktorexpand.code.network

import io.ktor.client.content.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream


suspend fun testFileDownload(fileName: String) {
    requestScope<File>(io = {
        sendKtorDownload(url = "http://192.168.51.60:9998/fs/$fileName")
    }, onSucceed = { body, _ ->
        println("body:${body.absolutePath}")
    }, onFailure = {}, onThrowable = {}, dispatcher = Dispatchers.IO)
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
suspend fun fileUpload(file: File, listener: ProgressListener) = ktorClient().use {
    val url = "http://192.168.51.60:9998/upload-json"
    it.post(url) {
        setBody(MultiPartFormDataContent(formData {
            append("file",
                file.readBytes(),
                headers = Headers.build { //            append(HttpHeaders.ContentType, ContentType.MultiPart.FormData)
                    append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo3.jpg\"")
                })
            onUpload(listener)
        }))
    }
}

suspend fun multiFileUpload(
    multiPartBody: List<MultiPartBody>,
    listener: (suspend (bytesSentTotal: Long, contentLength: Long) -> Unit)?
) = ktorClient().use {
    val url = "http://192.168.1.240:9998/multipart-upload-json"

    it.post(url) {
        setBody(MultiPartFormDataContent(formData {
            for (multiPart in multiPartBody) {
                append(multiPart.key,
                    multiPart.file.readBytes(),
                    headers = Headers.build { //                  append(HttpHeaders.ContentType, ContentType.MultiPart.FormData)
                        if (multiPart.contentDisposition == EmptyString) {
                            append(
                                HttpHeaders.ContentDisposition, "filename=${multiPart.file.name}"
                            )
                        } else {
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=${multiPart.contentDisposition}"
                            )
                        }
                    })
            }
        })) //多文件上传只能监听总进度问题
        onUpload(listener)

    }
}


fun main() = runBlocking {
    val outFile = File("/Library/MyComputer/Software/Android/Git/ViewModelAutomation/test.jpg")
    val outFile2 = File("/Library/MyComputer/Software/Android/Git/ViewModelAutomation/2.jpeg")

    println("exist:${outFile.exists()},${outFile.name}") //fileDownload(outputStream)

    //文件上传
    /* launch {
 //        testFileDownload("1.jpg", outFile)
         val fileUpload = fileUpload(outFile) { a, b ->
             println("current:$a,total:$b")
         }
         println("http:${fileUpload.status}")
     }*/ //多文件上传:312841\169675
    launch {
        val httpResponse = multiFileUpload(
            listOf(MultiPartBody("file", outFile, "ktor1.png") { a, b ->
                println("ktor1.png process:${a}")
            }, MultiPartBody("file", outFile2, "ktor2.png") { a, b ->
                println("ktor2.png process:${a}")
            }),
        ) { a, b ->

        }
        println("http:${httpResponse.status}")
    }

    println("end...:")
}