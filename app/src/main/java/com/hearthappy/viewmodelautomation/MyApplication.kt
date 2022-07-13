package com.hearthappy.viewmodelautomation

import android.app.Application
import com.hearthappy.annotations.Service
import com.hearthappy.annotations.ServiceConfig

@Service
@ServiceConfig(baseURL = "http://192.168.30.200", proxyIp = "http://www.baidu.com", proxyPort = 3389)
@ServiceConfig(key = "server", baseURL = "http://192.168.30.69", proxyIp = "http://www.baidu.com", proxyPort = 3389, enableLog = false)
class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
