package com.hearthappy.ktorexpand.code.network

import io.ktor.http.*

data class Append(val key:String,val value:Any,val headers: Headers = Headers.Empty)
