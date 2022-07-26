package com.hearthappy.annotations


/**
 * 基础配置，将注解的相关参数、值，生成在sendKtorRequest请求中的配置，固定不可变
 * @property baseURL String
 * @property enableLog Boolean
 * @property proxyIP String
 * @property proxyPort Int
 * @property key String
 * @constructor
 */
@Repeatable @Target(AnnotationTarget.CLASS) @Retention(AnnotationRetention.SOURCE)
annotation class ServiceConfig(val baseURL: String, val enableLog: Boolean = true, val proxyIP: String = "", val proxyPort: Int = -1, val key: String = "defaultConfig")
