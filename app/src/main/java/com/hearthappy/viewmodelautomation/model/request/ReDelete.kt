package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.*

@Headers([ContentType.Application.Json,ContentType.Application.FormUrlEncoded])
@Request(type = RequestType.DELETE, urlString = "/veaudit/v2/vir_msg/{instance_id}")
data class ReDelete(@Header("X-Auth-Token") val token: String,val instance_id:String)
