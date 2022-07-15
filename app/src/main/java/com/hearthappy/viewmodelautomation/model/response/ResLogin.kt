package com.hearthappy.viewmodelautomation.model.response

/**
 * Created Date 2020/11/30.
 *
 * @author ChenRui
 * ClassDescription:new login intface
 */
data class ResLogin(
    val group_id: String,
    val header: String,
    val id: String,
    val is_initial: Boolean,
    val refresh_expires: Int,
    val refresh_token: String,
    val token: String,
    val token_expires: Int
)