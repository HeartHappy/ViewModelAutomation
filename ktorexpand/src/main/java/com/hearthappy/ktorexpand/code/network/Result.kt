package com.hearthappy.ktorexpand.code.network

import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.ktor.client.statement.*

/**
 * A generic class that holds a value with its loading status.
 * @param <T> LiveData
 */
sealed class Result<out T: Any> {

    data class Success<out T: Any>(val data: T): Result<T>()
    data class Error(val message: FailedBody): Result<Nothing>()
    data class Throwable(val e: kotlin.Throwable): Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[msg=$message]"
            is Throwable -> "Throwable[e=$e]"
        }
    }
}


suspend inline fun <reified R> resultHandler(result: kotlin.Result<HttpResponse>, crossinline onSucceed: (R) -> Unit, crossinline onFailure: (FailedBody) -> Unit, crossinline onThrowable: (Throwable) -> Unit) {
    result.apply {
        result.exceptionOrNull()?.let(onThrowable) ?: let {
            getOrNull()?.let { response ->
                if (this.isSuccess && response.status.value == 200) {
                    val readText = response.readText()
                    println("HttpClient---> Result onSucceed:${response.status.value},$readText")
                    when {
                        isJson(readText) && R::class.java.name != String::class.java.name -> {
                            println("HttpClient---> Result isJson:${R::class.java},${R::class.java.name}")
                            val fromJson = Gson().fromJson(readText, R::class.java)
                            onSucceed(fromJson)
                        }
                        !isJson(readText) && R::class.java.name == String::class.java.name -> {
                            onSucceed(readText as R)
                        }
                        else -> {
                            println("HttpClient---> Parsing error, response text: $readText, does not match response type: ${R::class.java}")
                            onThrowable(JsonParseException("Parsing error, response text: $readText, does not match response type: ${R::class.java}"))
                        }
                    }
                } else {
                    val failedBody = FailedBody(response.status.value, response.readText())
                    println("HttpClient---> Result failed:$failedBody")
                    onFailure(failedBody)
                }
            }
        }
    }
}


/**
 * 判断string字符串是不是json格式
 * @param content
 * @return
 */
fun isJson(content: String): Boolean {
    val jsonFormat = arrayListOf("{", "}", ":", "\"")
    var isJson = false
    jsonFormat.forEach {
        isJson = content.contains(it)
        if (!isJson) return@forEach
    }

    return if (content.contains("[") && content.contains("]")) {
        true
    } else isJson
}


