package com.hearthappy.viewmodelautomation.model

import com.hearthappy.annotations.BaseUrl

@BaseUrl
var serverBaseUrl: String = "http://192.168.51.23:50000"

@BaseUrl(key = "server")
var server3BaseUrl: String = "http://192.168.50.40"