package com.hearthappy.annotations

@Target(AnnotationTarget.CLASS) @Retention(AnnotationRetention.SOURCE)
annotation class Headers(val headers: Array<String>)
