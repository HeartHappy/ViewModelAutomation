package com.hearthappy.processor.model

import com.hearthappy.annotations.RequestType

data class RequestData(
    val requestClass: String,
    val requestType: RequestType,
    val url: String,
    val serviceConfigData: ServiceConfigData?,
    val headers: List<HeaderData>,
    val fixedHeaders: List<String>?,
    val methodParameters: List<ParameterData>,
    val requestParameters: List<String>,
    val requestBodyData: RequestBodyData
)
