package com.hearthappy.viewmodelautomation.model.response

data class ResLogin(
    val code: Int,
    val message: String,
    val result: Result?
) {
    data class Result(
        val account: String,
        val createdAt: String,
        val deletedAt: Any,
        val id: Int,
        val level: Int,
        val token: String,
        val updatedAt: String
    )
}