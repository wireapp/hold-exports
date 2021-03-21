package com.wire.integrations.hold.exports.export

import com.wire.integrations.hold.exports.dto.EnrichedEvent
import java.util.UUID

interface Exporter {
    /**
     * Exports data from events.
     *
     * Returns set of message ids, that were successfully exported.
     */
    fun export(events: Iterable<EnrichedEvent>): Set<UUID>
}
