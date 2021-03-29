package com.wire.integrations.hold.exports

import com.wire.integrations.hold.exports.dto.EnrichedEvent
import com.wire.integrations.hold.exports.utils.MessageId

/**
 * Interface that gives an view on what should each exporter implement.
 */
interface Exporter {
    /**
     * Exports data from events. Should not delete the original files from assets.
     *
     * Returns set of message ids, that were successfully exported.
     */
    fun export(events: Iterable<EnrichedEvent>): Set<MessageId>
}
