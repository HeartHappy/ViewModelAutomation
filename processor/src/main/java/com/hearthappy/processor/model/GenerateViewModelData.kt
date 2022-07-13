package com.hearthappy.processor.model

import com.squareup.kotlinpoet.ClassName

data class GenerateViewModelData(
    val viewModelClassName:String,
    val requestBody: ClassName,
    val responseBody: ClassName,
    val pubPropertyName: String,
    val priPropertyName: String
)