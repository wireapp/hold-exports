package com.wire.integrations.hold.exports

import com.wire.integrations.hold.exports.dto.ApplicationInfo
import com.wire.integrations.hold.exports.dto.RawEvent
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import org.kodein.di.instance

/**
 * Install routing feature.
 */
fun Application.installRouting() = routing {
    val info by di.instance<ApplicationInfo>()
    val exportService by di.instance<ExportService>()

    get("/export") {
        val event = call.receive<RawEvent>()
        // todo auth
        exportService.export(event)
        call.respond(HttpStatusCode.OK)
    }

    /**
     * Information about service.
     */
    get("/") {
        call.respond("Server running version: \"${info.version}\".")
    }

    /**
     * Send data about version.
     */
    get("/version") {
        call.respond(mapOf("version" to info.version))
    }

    /**
     * Responds only 200 for ingres.
     */
    get("/status") {
        call.respond(HttpStatusCode.OK)
    }
}
