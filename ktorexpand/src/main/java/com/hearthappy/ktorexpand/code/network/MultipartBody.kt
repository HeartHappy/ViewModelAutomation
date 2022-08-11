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

    var contentLength: Long = 0

    /**
     * 单一文件上传
     * @constructor
     */
    class Part(block: Part.() -> Unit) : MultipartBody() {
        init {
            block()
        }
    }

    /**
     * 多文件上传
     * @constructor
     */
    class MultiPart(block: MultiPart.() -> Unit) : MultipartBody() {
        init {
            multiPartData = arrayListOf()
            block()
        }
    }

    fun MultiPart.part(key: String, file: File, contentDisposition: String? = null, contentType: ContentType = ContentType.MultiPart.FormData) {
        contentLength += file.length()
        multiPartData?.add(PartData(key, file, contentDisposition, contentType))
    }

    fun MultiPart.part(key: String, file: File, headers: Headers = Headers.Empty) {
        contentLength += file.length()
        multiPartData?.add(PartData(key = key, file = file, headers = headers))
    }

    fun Part.part(key: String, file: File, contentDisposition: String? = null, contentType: ContentType = ContentType.MultiPart.FormData) {
        contentLength = file.length()
        partData = PartData(key, file, contentDisposition, contentType)
    }

    fun Part.part(key: String, file: File, headers: Headers = Headers.Empty) {
        contentLength = file.length()
        partData = PartData(key = key, file = file, headers = headers)
    }
}

/**
 * 使用 append 函数添加到formData
 * @param block [@kotlin.ExtensionFunctionType] Function1<MultipartBody, Unit>
 * @return MultipartBody
 */
fun MultipartBody.formData(block: MultipartBody.() -> Unit): MultipartBody {
    appends ?: apply { appends = arrayListOf() }
    block()
    return this
}

fun MultipartBody.append(key: String, value: Any, headers: Headers = Headers.Empty) {
    appends?.add(Append(key, value, headers))
}

fun MultipartBody.boundary(boundary: String) {
    this.boundary = boundary
}


/**
 * 文件相关参数
 * @property key String
 * @property file File
 * @property contentDisposition String? 注意：如果您传入了Headers构建参数，则不会在使用 contentDisposition 和 contentType 参数。因为这两个属性是headers中的一部分
 * @property contentType ContentType 同contentDisposition
 * @property headers Headers
 * @constructor
 */
internal data class PartData(val key: String, val file: File, val contentDisposition: String? = null, val contentType: ContentType = ContentType.MultiPart.FormData, val headers: Headers = Headers.Empty)
