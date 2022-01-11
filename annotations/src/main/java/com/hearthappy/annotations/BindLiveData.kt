package com.hearthappy.annotations

import kotlin.reflect.KClass

/**
 * @Author ChenRui
 * @Date   2021/12/23-11:05
 * @Email  1096885636@qq.com
 * ClassDescription :LiveData注解
 */


@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class BindLiveData(val methodName: String, //方法名称
                              val requestClass: KClass<*> = Any::class, //url类
                              val responseClass: KClass<out Any>, //网络响应的数据类型
                              val liveDataName: String = "" //需要创建的liveData名称
)
