package com.wire.integrations.hold.exports.server

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.wire.integrations.hold.exports.di
import com.wire.integrations.hold.exports.monitoring.CALL_ID
import com.wire.integrations.hold.exports.monitoring.PATH
import com.wire.integrations.hold.exports.monitoring.REMOTE_HOST
import com.wire.integrations.hold.exports.utils.createLogger
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.ForwardedHeaderSupport
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.features.callId
import io.ktor.jackson.jackson
import io.ktor.request.httpMethod
import io.ktor.request.path
import org.slf4j.event.Level
import java.util.UUID

private val installationLogger = createLogger("ApplicationSetup")

/**
 * Loads the application.
 */
fun Application.init() {
    di
    installationLogger.debug { "DI container started." }
    installBasics()
    installMonitoring()
    installRouting()
}

// Install basic extensions and necessary features to the Ktor.
private fun Application.installBasics() {
    // default headers
    install(DefaultHeaders)
    // initialize Jackson
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            // use ie. 2021-03-15T13:55:39.813985Z instead of 1615842349.47899
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    // as we're running behind the proxy, we take remote host from X-Forwarded-From
    install(XForwardedHeaderSupport)
    install(ForwardedHeaderSupport)
}

// Install monitoring features and call logging.
private fun Application.installMonitoring() {
    // requests logging in debug mode + MDC tracing
    install(CallLogging) {
        // put useful information to log context
        mdc(CALL_ID) { it.callId }
        mdc(REMOTE_HOST) { it.request.determineRealIp() }
        mdc(PATH) { "${it.request.httpMethod.value} ${it.request.path()}" }
        filter {
            !it.request.path().endsWith("status")
        }

        level = Level.DEBUG
        logger = createLogger("HttpCallLogger")
        format {
            "${it.request.determineRealIp()}: ${it.request.httpMethod.value} ${it.request.path()} -> " +
                    "${it.response.status()?.value} ${it.response.status()?.description}"
        }
    }
    // MDC call id setup
    install(CallId) {
        retrieveFromHeader("X-Request-Id")
        generate { UUID.randomUUID().toString() }
    }
}

