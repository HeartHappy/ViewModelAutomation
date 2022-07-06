package com.hearthappy.viewmodelautomation.model

import com.hearthappy.annotations.BaseConfig
import kotlin.properties.Delegates

@BaseConfig
var serverBaseUrl by Delegates.notNull<String>()

@BaseConfig(key = "server", proxyIp = "192.168.51.212", proxyPort = 3389)
var server3BaseUrl: String = "http://192.168.50.40"




