package com.wire.integrations.hold.exports

import com.wire.integrations.hold.exports.dto.EnrichedEvent
import com.wire.integrations.hold.exports.dto.ExportResult

/**
 * Interface that gives an view on what should each exporter implement.
 */
interface Exporter {
    /**
     * Exports data from events. Should not delete the original files from assets.
     *
     * Returns export result for each message.
     */
    fun export(events: Iterable<EnrichedEvent>): Set<ExportResult>

    /**
     * Exports data from given event. Should not delete the original files from assets.
     *
     * Returns export result for the event.
     */
    fun export(event: EnrichedEvent): ExportResult
}
