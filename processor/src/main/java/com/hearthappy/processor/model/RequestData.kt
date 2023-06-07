package com.hearthappy.processor.model

import com.hearthappy.annotations.Http

/**
 *
 * @property requestClass String 当前请求类
 * @property http Http 请求类型
 * @property url String 请求url
 * @property serviceConfigData ServiceConfigData? 当前请求服务配置
 * @property headers List<HeaderData> 动态头
 * @property fixedHeaders List<String>? 固定头
 * @property methodParameters List<ParameterData> 方法参数
 * @property requestParameters List<String> get 参数
 * @property requestBodyData RequestBodyData body 参数
 * @property order String? 排序
 * @property streamingParameter ParameterData? 流参数
 * @property multiPartParameters List<ParameterData>? 上传文件
 * @constructor
 */
data class RequestData(val requestClass: String, val http: Http, val url: String, val serviceConfigData: ServiceConfigData?, val headers: List<HeaderData>, val fixedHeaders: List<String>?, var methodParameters: List<ParameterData>, val requestParameters: List<String>, val requestBodyData: RequestBodyData, val order: String?, var streamingParameter: ParameterData?, val multiPartParameters: List<ParameterData>?, val cookies: List<CookieData>)
