package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.*

@Request(RequestType.POST, urlString = "/identity/v3/auth/tokens/")
@Body(BodyType.X_WWW_FormUrlEncoded)
data class ReUserInfo(@Query("username")val username: String, @Query("password") val password: String)
