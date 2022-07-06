package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.Header
import com.hearthappy.annotations.Request
import com.hearthappy.annotations.RequestType


@Request(urlString = "/identity/v3/users/{username},{password}", type = RequestType.GET,baseUrlKey = "server")
data class ReRegister(@Header("X-Auth-Token") val token: String, @Header("Content-Type") val contentType: String, val username: String, val password: String, val age: Int)
