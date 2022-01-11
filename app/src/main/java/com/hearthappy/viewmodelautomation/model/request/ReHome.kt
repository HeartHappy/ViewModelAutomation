package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.Header
import com.hearthappy.annotations.Request
import com.hearthappy.annotations.RequestType

@Request(type = RequestType.FormUrlEncoded, "http://192.168.51.212:50006/c-api/user-login-pwd")
data class ReHome(@Header("X-Auth-Token") val token: String, val username: String, val password: String)
