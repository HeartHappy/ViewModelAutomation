package com.hearthappy.ktorexpand.code.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


inline fun <reified R> ViewModel.requestScope(
    crossinline io: suspend () -> HttpResponse,
    crossinline onSucceed: (R, HttpResponse) -> Unit,
    crossinline onFailure: (FailedBody) -> Unit,
    crossinline onThrowable: (Throwable) -> Unit = {
        println("HttpClient---> Result throwable:$it")
    }
) {
    viewModelScope.launch(Dispatchers.IO) {
        runCatching { io() }.apply { resultHandler(this, onSucceed, onFailure, onThrowable) }
    }
}

suspend inline fun <reified R> requestScope(
    crossinline io: suspend () -> HttpResponse,
    crossinline onSucceed: (R, HttpResponse) -> Unit,
    crossinline onFailure: (FailedBody) -> Unit,
    crossinline onThrowable: (Throwable) -> Unit = {
        println("HttpClient---> Result throwable:$it")
    }
) {
    runCatching { io() }.apply { resultHandler(this, onSucceed, onFailure, onThrowable) }
}


sealed class RequestState<out T> {
    object LOADING : RequestState<Nothing>()
    data class SUCCEED<T>(val body: T, val response: HttpResponse) : RequestState<T>()
    data class FAILED(val failedBody: FailedBody) : RequestState<Nothing>()
    data class Throwable(val throwable: kotlin.Throwable) : RequestState<Nothing>()
    object DEFAULT : RequestState<Nothing>()
}



