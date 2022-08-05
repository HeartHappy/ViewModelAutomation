package com.hearthappy.viewmodelautomation

import android.app.Application
import com.hearthappy.annotations.Service
import com.hearthappy.annotations.ServiceConfig

@Service
@ServiceConfig(baseURL = "https://api.apiopen.top/api")
@ServiceConfig(baseURL = "http://192.168.51.60", key = "download")
class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
