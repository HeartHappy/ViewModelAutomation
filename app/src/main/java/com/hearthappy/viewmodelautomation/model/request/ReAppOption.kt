package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.*

@Request(RequestType.POST, "/c-api/app-options/")
data class ReAppOption(
    @Header("Authorization") val token: String,
    @Body(BodyType.FormUrlEncoded) val data: ReAppOptionData
)

data class ReAppOptionData(
    @Query("app_id") val app_id: String, @Query("cluster_id") val cluster_id: String
)