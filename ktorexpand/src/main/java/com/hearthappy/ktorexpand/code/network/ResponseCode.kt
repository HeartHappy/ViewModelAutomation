package com.hearthappy.ktorexpand.code.network

import android.net.ParseException
import com.google.gson.JsonParseException
import com.hearthappy.ktorexpand.code.network.exception.HttpException
import org.json.JSONException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

const val SUCCEED = 200
const val BAD_REQUEST = 400
const val UNAUTHORIZED = 401
const val FORBIDDEN = 403
const val NOT_FOUND = 404
const val REQUEST_TIMEOUT = 408
const val INTERNAL_SERVER_ERROR = 500
const val BAD_GATEWAY = 502
const val SERVICE_UNAVAILABLE = 503
const val GATEWAY_TIMEOUT = 504

//异常码
const val UNKNOWN = 0
const val OTHER = 1

//请求状态码
const val NETWORK_ERROR = 100

internal fun exceptionToError(e: Throwable) = if (e is HttpException) {             //HTTP 错误
    httpCode(e.response.status,e)
} else if (e is JsonParseException || e is JSONException || e is ParseException) {
    FailedBody(OTHER, "解析错误") //均视为解析错误
} else if (e is ConnectException || e is SocketTimeoutException) {
    FailedBody(OTHER, "连接超时，请检查网络或稍后重试！") //均视为网络错误，原因：1、本地网络问题  2、服务器问题 3、路由地址不对
} else if (e is SSLHandshakeException) {
    FailedBody(OTHER, "系统不信任其安全证书。出现此问题的原因可能是配置有误或您的连接被拦截了")
} else {
    FailedBody(UNKNOWN, e.toString()) //未知错误
}

internal fun httpCode(code: Int, e: HttpException): FailedBody {
    val errorMsg = when (code) {
        BAD_REQUEST -> "提交的字段类型和后台接收字段类型不匹配"
        UNAUTHORIZED -> "当前请求需要用户验证"
        FORBIDDEN -> "服务器已经理解请求，但是拒绝执行它"
        NOT_FOUND -> "服务器异常，请稍后再试"
        REQUEST_TIMEOUT -> "请求超时，请稍后再试"
        GATEWAY_TIMEOUT -> "作为网关或者代理工作的服务器尝试执行请求时，未能及时从上游服务器（URI 标识出的服务器，例如 HTTP、FTP、LDAP）或者辅助服务器（例如 DNS）收到响应"
        INTERNAL_SERVER_ERROR -> "服务器遇到了一个未曾预料的状况，导致了它无法完成对请求的处理"
        BAD_GATEWAY -> "作为网关或者代理工作的服务器尝试执行请求时，从上游服务器接收到无效的响应"
        SERVICE_UNAVAILABLE -> "由于临时的服务器维护或者过载，服务器当前无法处理请求"
        else -> e.message.toString() //其它均视为网络错误
    }
    return FailedBody(code, errorMsg)
}



