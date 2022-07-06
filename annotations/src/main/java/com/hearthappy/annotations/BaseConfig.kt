package com.hearthappy.annotations


/**
 * 基础配置，将注解的相关参数、值，生成在sendKtorRequest请求中的配置，固定不可变
 * @property key String
 * @property enableLog Boolean
 * @property proxyIp String
 * @property proxyPort Int
 * @constructor
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS,AnnotationTarget.PROPERTY) @Retention(AnnotationRetention.SOURCE)
annotation class BaseConfig(val key: String = "default", val enableLog: Boolean = true, val proxyIp: String = "", val proxyPort: Int = -1)
