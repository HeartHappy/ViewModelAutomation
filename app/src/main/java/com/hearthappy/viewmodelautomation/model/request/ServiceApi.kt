package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.Body
import com.hearthappy.annotations.Http
import com.hearthappy.annotations.Order
import com.hearthappy.annotations.Request


/**
 * GET:获取图片
 * @property page Int
 * @property size Int
 * @constructor
 */
@Order @Request(urlString = "/getImages") data class ReImages(val page: Int, val size: Int)


/**
 * GET:获取视频
 * @property page Int
 * @property size Int
 * @constructor
 */
@Request(urlString = "/getHaoKanVideo") data class ReVideoList(val page: Int, val size: Int)


/**
 * POST:登录
 * @property loginBody LoginBody
 * @constructor
 */
@Request(Http.POST, urlString = "/login") data class ReLogin(@Body val loginBody: LoginBody)

data class LoginBody(val account: String, val password: String)


/**
 * POST:发送验证码
 * @property mail String
 * @constructor
 */
@Request(Http.POST, urlString = "/sendVerificationCode") @Body
data class ReSendVerificationCode(val mail: String)


/**
 * POST:注册
 * @property account String
 * @property code String
 * @property password String
 * @constructor
 */
@Request(Http.POST, urlString = "/register") @Body
data class ReRegister(val account: String, val code: String, val password: String)


//@Streaming
@Request(Http.GET, urlString = "/fs/{fileName}", serviceKey = "download")
data class ReqDownloadFile(val fileName:String)
