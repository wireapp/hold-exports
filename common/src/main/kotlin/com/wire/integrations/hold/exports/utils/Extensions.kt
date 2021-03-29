package com.wire.integrations.hold.exports.utils

import mu.KLogging

/**
 * Creates logger for the current package.
 */
fun createLogger(name: String) = KLogging().logger("com.wire.integrations.hold.exports.${name}")

@PublishedApi
internal val catchingLogger = createLogger("extensions")

/**
 * Extensions that catches all errors during [transform] and maps results to not null list.
 */
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

