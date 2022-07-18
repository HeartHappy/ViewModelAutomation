package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.*

@Request(RequestType.GET, urlString = "/identity/v3/auth/tokens/")
@Body(BodyType.FormUrlEncoded)
data class ReUserInfo(@Query("username")val username: String, @Query("password") val password: String)
