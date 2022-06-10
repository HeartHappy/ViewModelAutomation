package com.hearthappy.ktorexpand.code.network

import android.annotation.SuppressLint
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import java.net.Proxy
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager


fun ktorClient(enableLog: Boolean = true,proxyConfig: Proxy= Proxy.NO_PROXY) = HttpClient(CIO) {
    expectSuccess = false //false：禁用，验证ResponseCode处理的异常，只有200为成功，其他的都会作为异常处理
    engine {
        threadsCount = 4
        pipelining = true

        //https认证
        /*https {
            // this: TLSConfigBuilder
            serverName = "api.ktor.io"
            cipherSuites = CIOCipherSuites.SupportedSuites
            trustManager = myCustomTrustManager
            random = mySecureRandom
            addKeyStore(myKeyStore, myKeyStorePassword)
        }*/
        proxy= proxyConfig
        //忽略https认证，可以使用https进行请求
        https {
            trustManager = @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)
            }
        }
    }





    install(JsonFeature) {
        serializer = GsonSerializer()
    }
    install(HttpTimeout) {
        val timeout = 5 * 1000L
        requestTimeoutMillis = timeout
        socketTimeoutMillis = timeout
        connectTimeoutMillis = timeout
    }

   /* install(Auth){
        bearer {
            
        }
    }*/

    if (enableLog) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }
    }
}


