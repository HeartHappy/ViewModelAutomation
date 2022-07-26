package com.hearthappy.annotations

import kotlin.reflect.KClass


/**
 * @Author ChenRui
 * @Date   2021/12/23-11:05
 * @Email  1096885636@qq.com
 * @property methodName String 方法名
 * @property requestClass KClass<*> 请求类
 * @property responseClass KClass<*> 响应类
 * @property liveDataName String 自定义生成liveData名称
 * @constructor
 */
@Repeatable @Target(AnnotationTarget.CLASS) @Retention(AnnotationRetention.SOURCE)
annotation class BindLiveData(val methodName: String, val requestClass: KClass<*> = Any::class, val responseClass: KClass<*> = String::class, val liveDataName: String = "")
