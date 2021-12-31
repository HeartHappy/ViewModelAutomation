package com.hearthappy.annotations

/**
 * @Author ChenRui
 * @Date   2021/12/27-16:28
 * @Email  1096885636@qq.com
 * ClassDescription :ViewModel注释解析器
 * viewModelClassName：创建的类名
 * viewModelType:ViewModel类型
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ViewModel(val viewModelClassName: String)
