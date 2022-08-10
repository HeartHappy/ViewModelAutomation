package com.hearthappy.ktorexpand.code.network

import io.ktor.http.*
import java.io.File

/**
 * 多文件上传
 * @property partData List<Part>
 * @property boundary String
 * @constructor
 */
open class MultipartBody( var partData: PartData? = null,  var multiPartData: ArrayList<PartData>? = null, val boundary: String = generateBoundary()) {


    companion object : MultipartBody() {
//        var partData: PartData? = null
//        var multiPartData: ArrayList<PartData>? = null

        fun part(block: () -> PartData): MultipartBody {
            partData?.apply { partData = null }
            partData = block()
            return this
        }

        fun multiPart(block: (ArrayList<PartData>) -> Unit): MultipartBody {
            this.multiPartData?.takeIf { it.isNotEmpty() }?.apply { this.clear() } ?: also { multiPartData = arrayListOf() }
            multiPartData?.apply(block)
            return this
        }
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
data class PartData(val key: String, val file: File, val contentDisposition: String? = null, val mediaType: ContentType = ContentType.MultiPart.FormData)
