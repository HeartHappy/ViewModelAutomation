package com.hearthappy.viewmodelautomation.model.response

data class ResVideoList(
    val code: Int, val message: String, val result: Result
) {
    data class Result(
        val list: List<Data>, val total: Int
    ) {
        data class Data(
            val coverUrl: String,
            val duration: String,
            val id: Int,
            val playUrl: String,
            val title: String,
            val userName: String,
            val userPic: String
        )
    }
}