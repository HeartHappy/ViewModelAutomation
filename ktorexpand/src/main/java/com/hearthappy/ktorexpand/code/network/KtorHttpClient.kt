package com.hearthappy.ktorexpand.code.network

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.gson.*
import java.net.InetSocketAddress
import java.net.Proxy


fun ktorClient(defaultConfig: DefaultConfig = DefaultConfig(EmptyString)) = HttpClient(OkHttp) {
    expectSuccess = false //false：禁用，验证ResponseCode处理的异常，只有200为成功，其他的都会作为异常处理
    engine {
        threadsCount = 4
        pipelining = false

        if (defaultConfig.proxyIP != EmptyString && defaultConfig.proxyPort != -1) {
            proxy = ProxyConfig(
                Proxy.Type.HTTP,
                InetSocketAddress(defaultConfig.proxyIP, defaultConfig.proxyPort)
            )
        } //https认证
        /*https {
            // this: TLSConfigBuilder
            serverName = "api.ktor.io"
            cipherSuites = CIOCipherSuites.SupportedSuites
            trustManager = myCustomTrustManager
            random = mySecureRandom
            addKeyStore(myKeyStore, myKeyStorePassword)
        }*/

        //忽略https认证，可以使用https进行请求
        /*https {
            trustManager = @SuppressLint("CustomX509TrustManager") object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)
            }
        }*/
    }




    install(ContentNegotiation) {
        gson()
    }

    /*install(HttpTimeout) {
        val timeout = 20 * 1000L
        requestTimeoutMillis = timeout
        socketTimeoutMillis = timeout
        connectTimeoutMillis = timeout
    }*/

    /* install(Auth){
         bearer {

         }
     }*/

    if (defaultConfig.enableLog) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }
}


