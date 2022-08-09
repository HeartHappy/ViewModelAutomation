package com.hearthappy.ktorexpand.code.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * ViewModel生成使用
 * @receiver ViewModel
 * @param io SuspendFunction0<HttpResponse>
 * @param onSucceed Function2<R, HttpResponse, Unit>
 * @param onFailure Function1<FailedBody, Unit>
 * @param onThrowable Function1<Throwable, Unit>
 * @param outFile File?
 * @param fileOutputStream FileOutputStream?
 * @param dispatcher CoroutineDispatcher
 */
inline fun <reified R : Any> ViewModel.requestScope(crossinline io: suspend () -> HttpResponse, crossinline onSucceed: (R, HttpResponse) -> Unit, crossinline onFailure: (FailedBody) -> Unit, crossinline onThrowable: (Throwable) -> Unit, outFile: File? = null, fileOutputStream: FileOutputStream? = null, dispatcher: CoroutineDispatcher = Dispatchers.Main) {
    viewModelScope.launch(Dispatchers.IO) {
        requestHandler(io, onSucceed, onFailure, onThrowable, dispatcher, outFile, fileOutputStream) //runCatching { io() }.apply { resultHandler(this, onSucceed, onFailure, onThrowable) }
    }
}


/**
 * 测试使用
 * @param io Function0<HttpResponse>
 * @param onSucceed Function2<R, HttpResponse, Unit>
 * @param onFailure Function1<FailedBody, Unit>
 * @param onThrowable Function1<Throwable, Unit>
 * @param outFile File?
 * @param fileOutputStream FileOutputStream?
 * @param dispatcher CoroutineDispatcher
 */
suspend inline fun <reified R> requestScope(io: () -> HttpResponse, crossinline onSucceed: (R, HttpResponse) -> Unit, crossinline onFailure: (FailedBody) -> Unit, crossinline onThrowable: (Throwable) -> Unit, outFile: File? = null, fileOutputStream: FileOutputStream? = null, dispatcher: CoroutineDispatcher = Dispatchers.Main) {
    runCatching { io() }.apply { resultHandler(this, onSucceed, onFailure, onThrowable, dispatcher, outFile, fileOutputStream) }
}


sealed class RequestState<out T> {
    object LOADING : RequestState<Nothing>()
    data class SUCCEED<T>(val body: T, val response: HttpResponse, val order: Int = InSitu) : RequestState<T>()
    data class FAILED(val failedBody: FailedBody, val order: Int = InSitu) : RequestState<Nothing>()
    data class Throwable(val throwable: kotlin.Throwable, val order: Int = InSitu) : RequestState<Nothing>()
    object DEFAULT : RequestState<Nothing>()
}



