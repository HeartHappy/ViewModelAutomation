package com.hearthappy.annotations


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION) @Retention(AnnotationRetention.SOURCE)
annotation class Request(
    val type: RequestType = RequestType.GET,
    val urlString: String,
    val serviceKey: String = "defaultConfig"
)


enum class RequestType {
    GET, POST, PATCH, DELETE
}



