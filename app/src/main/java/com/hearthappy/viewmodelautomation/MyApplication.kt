package com.hearthappy.viewmodelautomation

import android.app.Application
import com.hearthappy.annotations.Service
import com.hearthappy.annotations.ServiceConfig

@Service
@ServiceConfig(baseURL = "https://api.apiopen.top/api")
//定义一个文件上传下载的服务地址
@ServiceConfig(baseURL = "http://192.168.51.62:9998", key = "fileOperate")
@ServiceConfig(baseURL = "http://192.168.31.206:8888", key = "Apifox")
class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
