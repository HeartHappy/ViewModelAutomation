package com.hearthappy.processor

import com.hearthappy.annotations.ContentType

// TODO: 固定头各类型的常量生成
const val CONTENT_TYPE = "Content-Type"

const val jsonHeader = "HttpHeaders.ContentType, ContentType.Application.Json"
const val formUrlEncodedHeader = "HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded"


/**
 * 获取固定头
 * @receiver String
 * @return String
 */
internal fun String?.asFixedHeader(): String {
    val key = this?.split(":")?.get(0)
    if (key == CONTENT_TYPE) {
        if (this == ContentType.Application.Json) {
            return jsonHeader
        } else if (this == ContentType.Application.FormUrlEncoded) {
            return formUrlEncodedHeader
        }
    }
    return jsonHeader
}