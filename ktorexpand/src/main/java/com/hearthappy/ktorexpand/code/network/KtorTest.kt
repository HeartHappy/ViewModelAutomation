package com.hearthappy.ktorexpand.code.network

import io.ktor.client.content.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.jvm.javaio.*
import io.ktor.utils.io.streams.*
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
    val url = "http://192.168.51.62:9998/upload-json"
    it.post(url) {
        setBody(MultiPartFormDataContent(formData {
            //from
            append("description", "Ktor logo")
            //file
            append("file", file.readBytes(), headers = Headers.build {
                append(HttpHeaders.ContentType, ContentType.Image.JPEG)
                append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo3.jpg\"")
//                append(HttpHeaders.ContentDisposition,ContentDisposition.Inline.withParameter(ContentDisposition.Parameters.Name, "file").withParameter(ContentDisposition.Parameters.FileName, "ktor_logo.png").toString())
            })
        }))
        onUpload(listener)
    }
}

suspend fun testFileUpload( multipartBody: MultipartBody,listener: suspend (bytesSentTotal: Long, contentLength: Long) -> Unit) {
    requestScope<String>(io = {
        sendKtorUpload(httpType = POST, url = "http://192.168.51.62:9998/upload-json", listener = listener, multipartBody = multipartBody)
    }, onFailure = { println("onFailure:${it.text}") }, onSucceed = { body, _ ->
        println("onSucceed:$body")
    }, onThrowable = {
        println("onThrowable:${it.message}")
    }, dispatcher = Dispatchers.IO)
}


suspend fun multiFileUpload(partData: List<PartData>, listener: (suspend (bytesSentTotal: Long, contentLength: Long) -> Unit)?) = ktorClient().use {
    val url = "http://192.168.1.240:9998/multipart-upload-json"

    it.post(url) {
        setBody(MultiPartFormDataContent(formData {
            for (multiPart in partData) {
                append(multiPart.key, multiPart.file.readBytes(), headers = Headers.build {
                    append(HttpHeaders.ContentType, ContentType.MultiPart.FormData)
                    if (multiPart.contentDisposition == EmptyString) {
                        append(HttpHeaders.ContentDisposition, "filename=${multiPart.file.name}")
                    } else {
                        append(HttpHeaders.ContentDisposition, "filename=${multiPart.contentDisposition}")
                    }
                })
            }
        })) //多文件上传只能监听总进度问题
        onUpload(listener)

    }
}


fun main() = runBlocking {
    val outFile = File("/Library/MyComputer/Software/Android/Git/ViewModelAutomation/test.jpeg")
//    val outFile2 = File("/Library/MyComputer/Software/Android/Git/ViewModelAutomation/2.jpeg")

    println("exist:${outFile.exists()},${outFile.name}") //fileDownload(outputStream)

    //文件上传
    /* launch {
 //        testFileDownload("1.jpg", outFile)
         val fileUpload = fileUpload(outFile) { a, b ->
             println("current:$a,total:$b")
         }
         println("http:${fileUpload.status}")
     }*/

    //多文件上传:312841\169675
    /* val url = "http://192.168.51.60:9998/upload-xml"
     var process = false
     launch {
         val sendKtorUpload = sendKtorUpload(url = url, part = Part("file", outFile), listener = { a, b ->
 //            val format = String.format("%.2f", a.toFloat() / b * 100)
             val current = (a.toFloat() / b * 100).toInt()
             if (!process) {
                 if (current == 100) process = true
                 println("process:${current}%")
             }
         })
         println("http:${sendKtorUpload.bodyAsText()}")
     }*/
    val multipartBody = MultipartBody.part { PartData("file", file = outFile, contentDisposition = "uploadFileName.png", mediaType = ContentType.Image.PNG) }
    testFileUpload(multipartBody){a,b->
        println("current:$a,total:$b")
    }

    println("end...:")
}