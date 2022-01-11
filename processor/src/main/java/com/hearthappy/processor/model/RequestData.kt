package com.hearthappy.processor.model

import com.hearthappy.annotations.RequestType

data class RequestData(val requestClass: String, val requestType: RequestType, val url: String, val headers: List<HeaderData>, val parameters: List<ParameterData>)
