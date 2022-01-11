package com.hearthappy.ktorexpand.code.network

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.hearthappy.ktorexpand.R
import com.hearthappy.ktorexpand.tools.CheckNetworkConnect
import com.hearthappy.ktorexpand.viewmodel.BaseAndroidViewModel
import kotlinx.coroutines.launch

private const val TAG = "RequestScope"


fun <T> BaseAndroidViewModel.requestScope(
    io: suspend () -> T,
    onSucceed: (T) -> Unit,
    onFailure: (failure: ResponseError) -> Unit,
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
        onFailure(ResponseError(NETWORK_ERROR, context.getString(R.string.please_check_network)))
    }
}



data class ResponseError(val errorCode: Int, val errorMsg: String)


sealed class RequestState {
    object LOADING : RequestState()
    data class SUCCEED<T>(val responseBody: T) : RequestState()
    data class FAILED(val message: ResponseError) : RequestState()
    object DEFAULT : RequestState()
}