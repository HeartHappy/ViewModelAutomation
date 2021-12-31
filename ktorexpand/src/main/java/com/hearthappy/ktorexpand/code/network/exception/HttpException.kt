package com.hearthappy.ktorexpand.code.network.exception

import io.ktor.http.cio.*

class HttpException(val response: Response) : RuntimeException("HTTP ${response.status}: ${response.statusText}")