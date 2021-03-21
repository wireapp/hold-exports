package com.wire.integrations.hold.exports.service

import com.wire.integrations.hold.exports.convert.AssetsFileDownloader
import com.wire.integrations.hold.exports.convert.EventParser
import com.wire.integrations.hold.exports.dao.RawEventsRepository
import com.wire.integrations.hold.exports.dto.ConversationEvent
import com.wire.integrations.hold.exports.dto.EnrichedEvent
import com.wire.integrations.hold.exports.utils.mapCatching
import mu.KLogging
import pw.forst.tools.katlib.mapToSet

class ProcessingService(
    private val assetsFileDownloader: AssetsFileDownloader,
    private val rawEventsRepository: RawEventsRepository,
    private val eventParser: EventParser
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

        val processed = toProcess.mapCatching(
            transform = { event ->
                eventParser.parse(event)?.let { event to it } // todo maybe log? but we log in parser
            },
            errorLog = {
                "Problem with parsing event ${it.messageId} with type ${it.type}."
            }
        ).mapCatching(
            transform = { (rawEvent, conversationEvent) ->
                when (conversationEvent) {
                    is ConversationEvent.SystemEvent -> {
                        EnrichedEvent.SystemEvent(
                            rawEvent = rawEvent,
                            conversationEvent = conversationEvent
                        )
                    }
                    is ConversationEvent.OtrEvent.Asset -> {
                        EnrichedEvent.AssetEvent(
                            rawEvent = rawEvent,
                            conversationEvent = conversationEvent,
                            savedFile = assetsFileDownloader.downloadAndSave(conversationEvent)
                        )
                    }
                    is ConversationEvent.OtrEvent -> {
                        EnrichedEvent.TextEvent(
                            rawEvent = rawEvent,
                            conversationEvent = conversationEvent
                        )
                    }
                }
            },
            errorLog = { (rawEvent, conversationEvent) ->
                """
                       ExportService: error while processing events.
                       Raw: $rawEvent
                       Conversation: $conversationEvent 
                """.trimIndent()
            }
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
