package com.hearthappy.ktorexpand.code.network

import io.ktor.client.content.*
import java.io.File

data class MultiPartBody(val key:String,val file:File,val contentDisposition:String= EmptyString,val listener: ProgressListener)
