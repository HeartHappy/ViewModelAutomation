package com.hearthappy.annotations

@Target(AnnotationTarget.VALUE_PARAMETER) @Retention(AnnotationRetention.SOURCE)
annotation class Field(val value: String)