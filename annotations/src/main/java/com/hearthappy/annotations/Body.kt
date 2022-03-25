package com.hearthappy.annotations

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Body(val bodyType: BodyType = BodyType.JSON)

enum class BodyType {
    NONE, TEXT, JSON, HTML, XML, FORM_DATA,X_WWW_FormUrlEncoded,
}


