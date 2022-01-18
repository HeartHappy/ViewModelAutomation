package com.hearthappy.processor.model

import com.hearthappy.annotations.RequestType

data class RequestData(val requestClass: String, val requestType: RequestType, val url: String, val baseUrlData: BaseUrlData?, val headers: List<HeaderData>, val methodParameters: List<ParameterData>, val requestParameters: List<String>,val requestBodyParameter:Any?)
