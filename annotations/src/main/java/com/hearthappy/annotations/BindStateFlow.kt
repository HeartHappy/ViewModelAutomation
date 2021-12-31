package com.hearthappy.annotations

import kotlin.reflect.KClass

/**
 * @Author ChenRui
 * @Date   2021/12/27-11:52
 * @Email  1096885636@qq.com
 * ClassDescription :
 */
@Repeatable @Target(AnnotationTarget.CLASS) @Retention(AnnotationRetention.BINARY)
annotation class BindStateFlow(
    val methodName: String, //方法名称
    val responseClass: KClass<*> = Any::class, //网络响应的数据类型
    val stateFlowName: String = "" //需要创建的liveData名称
)
