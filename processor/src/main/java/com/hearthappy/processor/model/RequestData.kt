package com.hearthappy.processor.model

import com.hearthappy.annotations.Http

data class RequestData(val requestClass: String, //当前请求类
    val http: Http, //请求类型
    val url: String, //请求url
    val serviceConfigData: ServiceConfigData?, //当前请求服务配置
    val headers: List<HeaderData>, //动态头
    val fixedHeaders: List<String>?, //固定头
    val methodParameters: List<ParameterData>, //方法参数
    val requestParameters: List<String>, //get 参数
    val requestBodyData: RequestBodyData, //body 参数
    val order: String?,   //时序
    var responseClass:String? = null)//响应类
