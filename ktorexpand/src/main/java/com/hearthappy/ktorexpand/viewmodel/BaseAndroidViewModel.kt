package com.hearthappy.ktorexpand.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BaseAndroidViewModel(app: Application) : AndroidViewModel(app) {

    internal val context: Context by lazy { getApplication<Application>().applicationContext }

    fun <T> MutableLiveData<T>.asLiveData() = this as LiveData<T>

    fun <T> MutableStateFlow<T>.asStateFlow() = this as StateFlow<T>
}