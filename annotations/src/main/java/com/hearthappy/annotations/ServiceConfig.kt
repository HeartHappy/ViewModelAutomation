package com.hearthappy.annotations


/**
 * 基础配置，将注解的相关参数、值，生成在sendKtorRequest请求中的配置，固定不可变
 * @property key String
 * @property baseURL String
 * @property enableLog Boolean
 * @property proxyIp String
 * @property proxyPort Int
 * @constructor
 */
@Repeatable @Target(AnnotationTarget.CLASS) @Retention(AnnotationRetention.SOURCE)
annotation class ServiceConfig(val key: String = "defaultConfig", val baseURL: String, val enableLog: Boolean = true, val proxyIp: String = "", val proxyPort: Int = -1)
