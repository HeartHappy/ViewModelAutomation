package com.hearthappy.processor.model

import com.hearthappy.annotations.RequestType

data class RequestData(val requestClass: String, val requestType: RequestType, val url: String, val baseConfigData: BaseConfigData?, val headers: List<HeaderData>, val methodParameters: List<ParameterData>, val requestParameters: List<String>, val requestBodyData: RequestBodyData)
