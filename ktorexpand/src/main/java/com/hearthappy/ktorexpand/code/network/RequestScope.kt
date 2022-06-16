package com.hearthappy.ktorexpand.code.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import io.ktor.client.statement.*
import io.ktor.http.parsing.*
import kotlinx.coroutines.launch


inline fun <reified R> ViewModel.requestScope(crossinline io: suspend () -> HttpResponse, crossinline onSucceed: (R) -> Unit, crossinline onFailure: (FailedBody) -> Unit, crossinline onThrowable: (Throwable) -> Unit = { println("HttpClient---> Result throwable:$it") }) {
    viewModelScope.launch {
        runCatching { io() }.apply { resultHandler(this, onSucceed, onFailure, onThrowable) }
    }
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

data class ErrorMessage(val error: String)


sealed class RequestState {
    object LOADING: RequestState()
    data class SUCCEED<T>(val responseBody: T): RequestState()
    data class FAILED(val failedBody: FailedBody): RequestState()
    data class Throwable(val throwable: kotlin.Throwable): RequestState()
    object DEFAULT: RequestState()
}

inline fun <reified T> RequestState.SUCCEED<*>.asSucceedBody(): T? {
    return try {
        this.responseBody as T
    } catch (e: Throwable) {
        throw RuntimeException("Conversion exception, the response result is: ${responseBody}, your constraint type is: ${T::class.java.name}")
    }
}

fun RequestState.FAILED.asFailedMessage(): ErrorMessage? {
    return try {
        Gson().fromJson(failedBody.text, ErrorMessage::class.java)
    }catch (e:Throwable){
        throw ParseException("Parse exception, text: ${failedBody.text}, does not match type ErrorMassage")
    }
}
