package com.wire.integrations.hold.exports

import com.wire.integrations.hold.exports.convert.EventParser
import com.wire.integrations.hold.exports.dto.RawEvent
import mu.KLogging

class ExportService(
    private val exporter: Exporter,
    private val parser: EventParser
) {
    private companion object : KLogging()

    /**
     * Exports given [event].
     */
    fun export(event: RawEvent) {
        parser.parse(event)
    }
}
