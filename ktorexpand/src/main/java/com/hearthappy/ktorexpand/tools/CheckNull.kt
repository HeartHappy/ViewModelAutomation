package com.hearthappy.ktorexpand.tools

inline fun <R> notNull(vararg args: Any?, block: () -> R) = when (args.filterNotNull().size) {
    args.size -> block()
    else -> null
}