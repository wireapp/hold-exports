package com.wire.integrations.hold.exports

import com.wire.integrations.hold.exports.dto.ExportResult
import com.wire.integrations.hold.exports.dto.RawEvent
import mu.KLogging

class ProcessingService(
    private val exporter: Exporter,
    private val eventEnricher: EventEnricher
) {
    private companion object : KLogging()

    /**
     * Exports given [rawEvent].
     */
    fun process(rawEvent: RawEvent): ExportResult =
        runCatching {
            eventEnricher.enrich(rawEvent)
        }.mapCatching {
            exporter.export(it)
        }.recover {
            ExportResult.Failure(rawEvent.messageId, it.message ?: "It was not possible to enrich event.")
        }.getOrThrow()
}
