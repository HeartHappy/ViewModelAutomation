package com.hearthappy.annotations


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION) @Retention(AnnotationRetention.SOURCE)
annotation class Request(
    val type: Http = Http.GET,
    val urlString: String,
    val serviceKey: String = "defaultConfig"
)


enum class Http {
    GET, POST, PATCH, DELETE
}



