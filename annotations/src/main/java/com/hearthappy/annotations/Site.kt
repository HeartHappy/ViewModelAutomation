package com.hearthappy.annotations

/**
 * 使用场景：多个相同请求，区分响应的先后顺序
 */
@Target(AnnotationTarget.VALUE_PARAMETER) @Retention(AnnotationRetention.SOURCE)
annotation class Site
