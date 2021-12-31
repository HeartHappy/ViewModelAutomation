package com.hearthappy.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Request(val urlString:String,val type:RequestType)

enum class RequestType{
    GET,
    POST,

}
