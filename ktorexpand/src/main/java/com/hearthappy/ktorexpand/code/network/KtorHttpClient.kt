package com.hearthappy.ktorexpand.code.network

import android.annotation.SuppressLint
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.http.*
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager


fun ktorClient(enableLog: Boolean = true) = HttpClient(CIO) {

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
            level = LogLevel.INFO
        }
    }
}


