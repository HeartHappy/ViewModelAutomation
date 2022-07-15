package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.*


//@Request(RequestType.POST, "/identity/v3/auth/tokens/")
//@Body
//data class ReLogin(
//    val username: String,
//    val password: String
//)
@Request(type = RequestType.POST, urlString = "/c-api/user-login-pwd") //如果@Body声明在类上，则参数无效，将ReLogin类作为body，注意：如包含请求头，请将@Body声明在参数上，指向另一个body
@Body(BodyType.FormUrlEncoded)
data class ReLogin(@Query("username") val username: String, @Query("password") val password: String)



