package com.hearthappy.viewmodelautomation.model.response

data class ResImages(
    val code: Int,
    val message: String,
    val result: Result
) {
    data class Result(
        val list: List<Data>,
        val total: Int
    ) {
        data class Data(
            val id: Int,
            val title: String,
            val type: String,
            val url: String
        )
    }
}