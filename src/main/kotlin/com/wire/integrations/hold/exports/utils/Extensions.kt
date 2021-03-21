package com.wire.integrations.hold.exports.utils

import mu.KLogging

fun createLogger(name: String) = KLogging().logger("com.wire.integrations.hold.exports.${name}")

val catchingLogger = createLogger("extensions")

inline fun <T, R> Iterable<T>.mapCatching(
    transform: (T) -> R?,
    crossinline errorLog: (T) -> String,
    logStackTrace: Boolean = false
): List<R> =
    mapNotNull { item ->
        runCatching { transform(item) }
            .onFailure {
                catchingLogger.error { errorLog(item) }
                if (logStackTrace) {
                    catchingLogger.debug(it) { }
                }
            }
            .getOrNull()
    }

