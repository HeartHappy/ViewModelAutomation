package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.Header
import com.hearthappy.annotations.Request
import com.hearthappy.annotations.RequestType


@Request(type = RequestType.GET, "/c-api/user-login-pwd", serviceKey = "server")
data class ReHome(@Header("X-Auth-Token") val token: String, val username: String, val password: String)
