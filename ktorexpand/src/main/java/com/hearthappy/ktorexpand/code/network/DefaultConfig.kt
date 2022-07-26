package com.hearthappy.ktorexpand.code.network

/**
 * 根据服务key创建
 * @property baseURL String 基础服务地址
 * @property proxyIP String 代理IP
 * @property proxyPort String 代理端口
 * @property enableLog Boolean 是否输出日志
 * @constructor
 */
data class DefaultConfig(var baseURL: String, var proxyIP: String = EmptyString, var proxyPort: Int = -1, var enableLog: Boolean = true) {

    fun setBaseURL(baseURL: String): DefaultConfig {
        this.baseURL = baseURL
        return this
    }

    fun setProxy(ip: String, port: Int): DefaultConfig {
        proxyIP = ip
        proxyPort = port
        return this
    }

    fun setEnableLog(enableLog: Boolean): DefaultConfig {
        this.enableLog = enableLog
        return this
    }
}