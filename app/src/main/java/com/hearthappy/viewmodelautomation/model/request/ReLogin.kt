package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.Body
import com.hearthappy.annotations.Header
import com.hearthappy.annotations.Request
import com.hearthappy.annotations.RequestType


@Request(type = RequestType.POST, urlString = "http://192.168.51.212:50006/c-api/user-login-pwd")
//如果@Body声明在类上，则参数无效，将ReLogin类作为body，注意：如包含请求头，请将@Body声明在参数上，指向另一个body

data class ReLogin(@Header("X-Auth-Token") val token: String, @Body val data: Data)

data class Data(val username: String, val password: String)

