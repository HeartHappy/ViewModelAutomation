package com.hearthappy.ktorexpand.code.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.RuntimeException

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
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


inline fun <reified R> ViewModel.requestHandler(crossinline io: suspend () -> HttpResponse, crossinline onSucceed: (R) -> Unit, crossinline onFailure: (FailedBody) -> Unit, crossinline onThrowable: (Throwable) -> Unit) {
    viewModelScope.launch {
        val result = runCatching {
            io()
        }
        result.exceptionOrNull()?.let(onThrowable) ?: let {
            result.getOrNull()?.let { response ->
                if (result.isSuccess && response.status.value == 200) {
                    val readText = response.readText()
                    println("HttpClient---> Result onSucceed:${response.status.value},$readText")
                    if (isJson(readText) && !R::class.java.name.contains("String")) {
                        println("HttpClient---> Result isJson:${R::class.java},${R::class.java.name}")
                        val fromJson = Gson().fromJson(readText, R::class.java)
                        onSucceed(fromJson)
                    } else {
                        println("HttpClient---> Json parsing error, please modify the response result: ${R::class.java}")
                        onThrowable(JsonParseException("Json parsing error, please modify the response result: ${R::class.java}"))
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

