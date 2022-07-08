package com.hearthappy.annotations


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION) @Retention(AnnotationRetention.SOURCE)
annotation class Request(
    val type: RequestType = RequestType.GET,
    val urlString: String,
    val baseUrlKey: String = "default"
)


enum class RequestType {
    GET, POST, PATCH, DELETE
}



