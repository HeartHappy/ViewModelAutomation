package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.Body
import com.hearthappy.annotations.BodyType
import com.hearthappy.annotations.Query
import com.hearthappy.annotations.Request

@Request(urlString = "/user/detail")
@Body(BodyType.X_WWW_FormUrlEncoded)
data class ReLoginUserInfo(@Query("token") val token: String = "")
