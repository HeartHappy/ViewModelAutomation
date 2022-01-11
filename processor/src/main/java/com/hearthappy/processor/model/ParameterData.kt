package com.hearthappy.processor.model

data class ParameterData(val parameterName: String, val parameterType: String)

enum class AnnotationType {
    PARAMETER, HEADER
}