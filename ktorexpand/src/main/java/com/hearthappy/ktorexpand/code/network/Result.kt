package com.hearthappy.ktorexpand.code.network

import com.google.gson.GsonBuilder
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.withContext

/**5
 * A generic class that holds a value with its loading status.
 * @param <T> LiveData
 */
sealed class Result<out T: Any> {

    data class Success<out T: Any>(val body: T, val site: Int = InSitu): Result<T>()
    data class Failed(val failedBody: FailedBody, val site: Int = InSitu): Result<Nothing>()
    data class Throwable(val throwable: kotlin.Throwable, val site: Int = InSitu): Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[body=$body]"
            is Failed -> "Failed[failedBody=$failedBody]"
            is Throwable -> "Throwable[throwable=$throwable]"
        }
    }
}

val succeedCode = 200..299

suspend inline fun <reified R> requestHandler(result: kotlin.Result<HttpResponse>, crossinline onSucceed: (R, HttpResponse) -> Unit, crossinline onFailure: (FailedBody) -> Unit, crossinline onThrowable: (Throwable) -> Unit, dispatcher: CoroutineDispatcher) {
    result.apply {
        result.exceptionOrNull()?.let(onThrowable) ?: let {
            getOrNull()?.let { response ->
                responseHandler(response, dispatcher, onSucceed, onFailure)
            }
        }
    }
}

suspend inline fun <reified R> requestHandler(crossinline io: suspend () -> HttpResponse, crossinline onSucceed: (R, HttpResponse) -> Unit, crossinline onFailure: (FailedBody) -> Unit, crossinline onThrowable: (Throwable) -> Unit, dispatcher: CoroutineDispatcher) {
    try {
        val response = io()
        responseHandler(response, dispatcher, onSucceed, onFailure)
    } catch (e: Throwable) {
        withMainCoroutine(dispatcher) { onThrowable(e) }
    }
}

suspend inline fun <reified R> responseHandler(response: HttpResponse, dispatcher: CoroutineDispatcher, crossinline onSucceed: (R, HttpResponse) -> Unit, crossinline onFailure: (FailedBody) -> Unit) {
    if (response.status.value in succeedCode) {
        println("HttpClient---> Result onSucceed:${response.status.value},Class:${R::class.java}")
        when {
            isString<R>() -> {
                val bodyString = response.bodyAsText()
                withMainCoroutine(dispatcher) { onSucceed(bodyString as R, response) }
            }
            else -> { //返回转换后对象类型
                val body = response.body<R>()
                withMainCoroutine(dispatcher) { onSucceed(body, response) }
            }
        }
    } else {
        val failedBody = FailedBody(response.status.value, response.bodyAsText())
        println("HttpClient---> Result failed:$failedBody")
        withMainCoroutine(dispatcher) { onFailure(failedBody) }
    }
}

suspend fun withMainCoroutine(dispatcher: CoroutineDispatcher, block: () -> Unit) {

    when (dispatcher) {
        is MainCoroutineDispatcher -> {
            withContext(Dispatchers.Main) { block() }
        }
        else -> block()
    }
}

inline fun <reified R> isString() = rIsString<R>()

inline fun <reified R> rIsString() = R::class.java.name == String::class.java.name


data class FailedBody(val statusCode: Int, val text: String?)

data class ErrorMessage(val error: String)


fun RequestState.FAILED.asFailedMessage(): String? {
    return failedBody.getFailedMessage()
}

fun Result.Failed.asFailedMessage(): String? {
    return failedBody.getFailedMessage()
}

fun RequestState.Throwable.asThrowableMessage(): String? {
    return this.throwable.message
}

fun Result.Throwable.asThrowableMessage(): String? {
    return this.throwable.message
}


private fun FailedBody?.getFailedMessage(): String? {
    return try { //如果响应的错误消息中包含error时，获取error消息，否则原文本返回
        this?.text?.let { GsonBuilder().create().fromJson(this.text, ErrorMessage::class.java).error }
    } catch (e: Throwable) {
        this?.text
    }
}




