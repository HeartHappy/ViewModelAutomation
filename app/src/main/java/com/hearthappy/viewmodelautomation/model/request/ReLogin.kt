package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.Body
import com.hearthappy.annotations.Header
import com.hearthappy.annotations.Request
import com.hearthappy.annotations.RequestType


//@Request(RequestType.POST, "/identity/v3/auth/tokens/")
//@Body
//data class ReLogin(
//    val username: String,
//    val password: String
//)

@Request(type = RequestType.PATCH, urlString = "/identity/v3/auth/tokens/{username},{password}") //如果@Body声明在类上，则参数无效，将ReLogin类作为body，注意：如包含请求头，请将@Body声明在参数上，指向另一个body
data class ReLogin(@Header("X-Auth-Token") val token: String, val username: String, val password: String, @Body val data: Data)

data class Data(val username: String, val password: String)

