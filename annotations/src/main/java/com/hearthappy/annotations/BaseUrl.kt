package com.hearthappy.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class BaseUrl(val key: String = "default")
