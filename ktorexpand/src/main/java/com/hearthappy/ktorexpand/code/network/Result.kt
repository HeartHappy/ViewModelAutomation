package com.hearthappy.ktorexpand.code.network

import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.ktor.client.statement.*
import io.ktor.http.parsing.*

/**
 * A generic class that holds a value with its loading status.
 * @param <T> LiveData
 */
sealed class Result<out T : Any> {

    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val message: FailedBody) : Result<Nothing>()
    data class Throwable(val e: kotlin.Throwable) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[msg=$message]"
            is Throwable -> "Throwable[e=$e]"
        }
    }
}

val succeedCode = 200..299

suspend inline fun <reified R> resultHandler(
    result: kotlin.Result<HttpResponse>,
    crossinline onSucceed: (R, HttpResponse) -> Unit,
    crossinline onFailure: (FailedBody) -> Unit,
    crossinline onThrowable: (Throwable) -> Unit
) {
    result.apply {
        result.exceptionOrNull()?.let(onThrowable) ?: let {
            getOrNull()?.let { response ->
                if (this.isSuccess && response.status.value in succeedCode) {
                    val readText = response.readText()
                    println("HttpClient---> Result onSucceed:${response.status.value},$readText")
                    when {
                        isJson<R>(readText) -> {
                            println("HttpClient---> Result isJson:${R::class.java},${R::class.java.name}")
                            val fromJson = Gson().fromJson(readText, R::class.java)
                            onSucceed(fromJson, response)
                        }
                        isString<R>(readText) -> {
                            onSucceed(readText as R, response)
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

inline fun <reified R> isString(readText: String) = !isJsonString(readText) && rIsString<R>()

inline fun <reified R> isJson(readText: String) = isJsonString(readText) && !rIsString<R>()

inline fun <reified R> rIsString() = R::class.java.name == String::class.java.name


data class FailedBody(val statusCode: Int, val text: String?)

data class ErrorMessage(val error: String)

inline fun <reified T> RequestState.SUCCEED<*>.asSucceedBody(): T? {
    return try {
        this.body as T
    } catch (e: Throwable) {
        throw RuntimeException("Conversion exception, the response result is: ${body}, your constraint type is: ${T::class.java.name}")
    }
}


fun RequestState.FAILED.asFailedMessage(): ErrorMessage? {
    return try {
        Gson().fromJson(failedBody.text, ErrorMessage::class.java)
    } catch (e: Throwable) {
        throw ParseException("Parse exception, text: ${failedBody.text}, does not match type ErrorMassage")
    }
}

/**
 * 判断string字符串是不是json格式
 * @param content
 * @return
 */
fun isJsonString(content: String): Boolean {
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


