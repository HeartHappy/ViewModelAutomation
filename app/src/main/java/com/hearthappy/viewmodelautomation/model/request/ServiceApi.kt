package com.hearthappy.viewmodelautomation.model.request

import com.hearthappy.annotations.*
import com.hearthappy.ktorexpand.code.network.MultipartBody


/**
 * GET:获取图片
 * @property page Int
 * @property size Int
 * @constructor
 */
@Order
@Request(urlString = "/getImages")
data class ReImages(val page: Int, val size: Int)


/**
 * GET:获取视频
 * @property page Int
 * @property size Int
 * @constructor
 */
@Request(urlString = "/getHaoKanVideo")
data class ReVideoList(val page: Int, val size: Int)


/**
 * POST:登录
 * @property loginBody LoginBody
 * @constructor
 */
@Request(Http.POST, urlString = "/login")
data class ReLogin(@Body val loginBody: LoginBody)

data class LoginBody(val account: String, val password: String)


/**
 * POST:发送验证码
 * @property mail String
 * @constructor
 */
@Request(Http.POST, urlString = "/sendVerificationCode")
@Body
data class ReSendVerificationCode(val mail: String)


/**
 * POST:注册
 * @property account String
 * @property code String
 * @property password String
 * @constructor
 */
@Request(Http.POST, urlString = "/register")
@Body
data class ReRegister(val account: String, val code: String, val password: String)

/**
 * 文件下载
 * @property fileName String
 * @constructor
 */
@Streaming
@Request(Http.GET, urlString = "/fs/{fileName}", serviceKey = "fileOperate")
data class ReqDownloadFile(val fileName: String)

/**
 * 文件上传
 * @property multipartBody MultipartBody
 * @constructor
 */
@Multipart
@Request(Http.POST, urlString = "/multipart-upload-json", serviceKey = "fileOperate")
data class ReUploadFile(val multipartBody: MultipartBody)

