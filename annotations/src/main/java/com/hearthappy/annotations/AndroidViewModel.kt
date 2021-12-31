package com.hearthappy.annotations

/**
 * @Author ChenRui
 * @Date   2021/12/27-17:31
 * @Email  1096885636@qq.com
 * ClassDescription :
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class AndroidViewModel(val viewModelClassName: String="")
