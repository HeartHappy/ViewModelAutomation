package com.hearthappy.ktorexpand.code.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


inline fun <reified R> ViewModel.requestScope(crossinline io: suspend () -> HttpResponse, crossinline onSucceed: (R, HttpResponse) -> Unit, crossinline onFailure: (FailedBody) -> Unit, crossinline onThrowable: (Throwable) -> Unit = {
    println("HttpClient---> Result throwable:$it")
}, dispatcher: CoroutineDispatcher = Dispatchers.Main) {
    viewModelScope.launch(Dispatchers.IO) {
        requestHandler(io, onSucceed, onFailure, onThrowable, dispatcher)

        //runCatching { io() }.apply { resultHandler(this, onSucceed, onFailure, onThrowable) }
    }
}


suspend inline fun <reified R> requestScope(io: () -> HttpResponse, crossinline onSucceed: (R, HttpResponse) -> Unit, crossinline onFailure: (FailedBody) -> Unit, crossinline onThrowable: (Throwable) -> Unit = {
    println("HttpClient---> Result throwable:$it")
}, dispatcher: CoroutineDispatcher = Dispatchers.Main) {
    runCatching { io() }.apply { requestHandler(this, onSucceed, onFailure, onThrowable, dispatcher) }
}


sealed class RequestState<out T> {
    object LOADING: RequestState<Nothing>()
    data class SUCCEED<T>(val body: T, val response: HttpResponse, val order: Int = InSitu): RequestState<T>()
    data class FAILED(val failedBody: FailedBody, val order: Int = InSitu): RequestState<Nothing>()
    data class Throwable(val throwable: kotlin.Throwable, val order: Int = InSitu): RequestState<Nothing>()
    object DEFAULT: RequestState<Nothing>()
}



