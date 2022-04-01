package com.hearthappy.annotations

@Target(AnnotationTarget.VALUE_PARAMETER) @Retention(AnnotationRetention.SOURCE)
annotation class Query(val value: String)