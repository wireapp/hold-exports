package com.wire.integrations.hold.exports.utils

import mu.KLogger
import mu.KLogging

val catchingLogger: KLogger
    get() = KLogging().logger("com.wire.integrations.hold.exports")

inline fun <T, R> Iterable<T>.mapCatching(transform: (T) -> R?, crossinline errorLog: (T) -> String): List<R> =
    mapNotNull { item ->
        runCatching { transform(item) }
            .onFailure {
                catchingLogger.error { errorLog(item) }
                catchingLogger.debug(it) { }
            }
            .getOrNull()
    }

