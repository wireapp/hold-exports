package com.wire.integrations.hold.exports.service

import com.wire.integrations.hold.exports.EventEnricher
import com.wire.integrations.hold.exports.dao.RawEventsRepository
import com.wire.integrations.hold.exports.dto.EnrichedEvent
import com.wire.integrations.hold.exports.utils.mapCatching
import mu.KLogging
import pw.forst.tools.katlib.mapToSet

class ProcessingService(
    private val rawEventsRepository: RawEventsRepository,
    private val enricher: EventEnricher
) {
    private companion object : KLogging()

    /**
     * Retrieves unprocessed events from the database.
     *
     * Downloads and saves all assets to temporary directory.
     */
    fun loadAndProcess(): List<EnrichedEvent> {
        val toProcess = rawEventsRepository.getNotExportedEvents()
        logger.debug { "To be processed: ${toProcess.size}." }
        // todo we can try to parallelize this
        val processed = toProcess.mapCatching(
            transform = { event ->
                enricher.enrich(event)
            },
            errorLog = {
                "Problem with parsing event ${it.messageId} with type ${it.type}."
            },
        )

        logger.debug { "Successfully processed entities ${processed.size}." }
        if (processed.size != toProcess.size) {
            val diff = toProcess.mapToSet { it.messageId }
                .subtract(processed.mapToSet { it.rawEvent.messageId })
            logger.warn { "Not all data survived processing! Difference was ${diff.size}. The following data:\n${diff.joinToString("\n")}" }
        }

        return processed
    }
}
