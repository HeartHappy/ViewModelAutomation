package com.hearthappy.processor.tools

import com.squareup.kotlinpoet.ClassName


/**
 * 如果字符串出现多次，只会返回第一个出现的
 * @receiver String
 * @param prefix String 前缀
 * @param suffix String 后缀
 * @param jumpOverCount Int 跳过个数
 * @param ignoreCount Int 忽略个数
 * @param missingDelimiterValue String 原有字符
 * @return String
 */
internal fun String.substringMiddle(prefix: String, suffix: String, jumpOverCount: Int = 0, ignoreCount: Int = 0, missingDelimiterValue: String = this): String {
    val prefixIndex = indexOf(prefix)
    val delPrefixBefore = if (prefixIndex == -1) missingDelimiterValue else substring(prefixIndex + jumpOverCount, length)
    val suffixIndex = delPrefixBefore.indexOf(suffix)

    return if (suffixIndex == -1) delPrefixBefore else delPrefixBefore.substring(0, suffixIndex + suffix.length - ignoreCount)
}


internal fun String.asRest(prefix: String, suffix: String): String {
    val removeSuffix = this.replace(suffix, "")
    return removeSuffix.replace(prefix, "\$")
}

internal fun String.findRest(list: List<String>): List<String> {
    return (list subtract (this.split(Regex("[{-}]")).filterNot { it.isEmpty() || it == "," })).toList()
}

internal fun String.removeWith(prefix: String, suffix: String): String {
    val substringMiddle = substringMiddle(prefix, suffix)
    return this.replace(substringMiddle, "")
}


/**
 * 拆分包，将全类名分割成包和类
 * @param fullClassName String
 * @return Pair<String, String>
 */
internal fun splitPackage(fullClassName: String): Pair<String, String> {
    val lastIndexOf = fullClassName.lastIndexOf(".")
    val packageName = fullClassName.subSequence(0, lastIndexOf)
    val className = fullClassName.subSequence(lastIndexOf + 1, fullClassName.length)
    return Pair(packageName.toString(), className.toString())
}

internal fun String.asKotlinClassName(): ClassName {
    val splitPackage = splitPackage(asKotlinPackage(this))
    return ClassName(splitPackage.first, splitPackage.second)
}


internal fun asKotlinPackage(javaPackage: String) = when (javaPackage) {
    in "java.lang.Object" -> "kotlin.Any"
    in "java.lang.String" -> "kotlin.String"
    in "int", "Int" -> "kotlin.Int"
    in "int[]" -> "kotlin.IntArray"
    in "long" -> "kotlin.Long"
    in "long[]" -> "kotlin.LongArray"
    in "boolean" -> "kotlin.Boolean"
    in "boolean[]" -> "kotlin.BooleanArray"
    in "float" -> "kotlin.Float"
    in "float[]" -> "kotlin.FloatArray"
    in "double" -> "kotlin.Double"
    in "double[]" -> "kotlin.DoubleArray"
    else -> javaPackage
}

