package com.hearthappy.ktorexpand.code.network

import io.ktor.http.*
import java.io.File


/**
 * 文件上传
 * @property partData PartData? 单文件上传
 * @property multiPartData ArrayList<PartData>? 多文件上传
 * @property boundary String
 * @constructor
 */
sealed class MultipartBody(internal var partData: PartData? = null, internal var multiPartData: ArrayList<PartData>? = null, internal var appends: ArrayList<Append>? = null, internal var boundary: String = generateBoundary()) {

    class Part(block: () -> PartData) : MultipartBody() {
        init {
            partData = block()
        }
    }

    class MultiPart(block: (ArrayList<PartData>) -> Unit) : MultipartBody() {
        init {
            multiPartData = arrayListOf()
            multiPartData?.apply(block)
        }
    }


    fun fromData(block: MultipartBody.() -> Unit): MultipartBody {
        block()
        return this
    }


    fun append(key: String, value: Any) {
        addToAppend(key, value)
    }

    private fun addToAppend(key: String, value: Any) {
        appends ?: apply { appends = arrayListOf() }
        appends?.add(Append(key, value))
        println("appends:${appends?.size}")
    }

    fun boundary(boundary: String) {
        this.boundary = boundary
    }

}


/**
 * 文件相关参数
 * @property key String
 * @property file File
 * @property contentType ContentType
 * @property contentDisposition String
 * @constructor
 */
data class PartData(val key: String, val file: File, val contentDisposition: String? = null, val contentType: ContentType = ContentType.MultiPart.FormData)
