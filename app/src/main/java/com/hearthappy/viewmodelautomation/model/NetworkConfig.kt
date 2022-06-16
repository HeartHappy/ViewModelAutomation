package com.hearthappy.viewmodelautomation.model

import com.hearthappy.annotations.BaseConfig

@BaseConfig
var serverBaseUrl: String = "http://192.168.51.23:50000"

@BaseConfig(key = "server",proxyIp = "192.168.51.212",proxyPort = 3389)
var server3BaseUrl: String = "http://192.168.50.40"


