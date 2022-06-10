package com.hearthappy.ktorexpand.code.network

import androidx.lifecycle.ViewModel
import io.ktor.client.statement.*



inline fun <reified R> ViewModel.requestScope(crossinline io: suspend () -> HttpResponse, crossinline onSucceed: (R) -> Unit, crossinline onFailure: (FailedBody) -> Unit, crossinline onThrowable: (Throwable) -> Unit = { println("HttpClient---> Result throwable:$it") }) {
    requestHandler(io, onSucceed, onFailure, onThrowable)
}

/*fun <T> BaseAndroidViewModel.requestScope(
    io: suspend () -> T,
    onSucceed: (T) -> Unit,
    onFailure: (failure: FailedBody) -> Unit,
) {
    if (CheckNetworkConnect.isNetworkConnected(context)) {
        viewModelScope.launch {
            kotlin.runCatching {
                io()
            }.onSuccess {
                Log.i(TAG, "requestScopeX: onSuccess->$it")
                onSucceed(it)
            }.onFailure {
                onFailure(exceptionToError(it))
            }
        }
    } else {
        onFailure(FailedBody(NETWORK_ERROR, context.getString(R.string.please_check_network)))
    }
}*/


data class FailedBody(val statusCode: Int, val text: String?)


sealed class RequestState {
    object LOADING: RequestState()
    data class SUCCEED<T>(val responseBody: T): RequestState()
    data class FAILED(val failedBody: FailedBody): RequestState()
    data class Throwable(val throwable: kotlin.Throwable): RequestState()
    object DEFAULT: RequestState()
}
