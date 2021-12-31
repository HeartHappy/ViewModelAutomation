package com.hearthappy.ktorexpand.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

open class BaseAndroidViewModel(app: Application) : AndroidViewModel(app) {

    internal val context: Context by lazy { getApplication<Application>().applicationContext }

}