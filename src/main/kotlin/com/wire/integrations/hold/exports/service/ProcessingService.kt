package com.wire.integrations.hold.exports.service

import com.wire.integrations.hold.exports.convert.AssetsFileDownloader
import com.wire.integrations.hold.exports.convert.EventParser
import com.wire.integrations.hold.exports.dao.RawEventsRepository
import com.wire.integrations.hold.exports.dto.ConversationEvent
import com.wire.integrations.hold.exports.dto.EnrichedEvent
import com.wire.integrations.hold.exports.utils.mapCatching
import mu.KLogging

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
    fun loadAndProcess(): List<EnrichedEvent> =
        rawEventsRepository.getNotExportedEvents().mapNotNull { event ->
            eventParser.parse(event)?.let { event to it } // todo maybe log? but we log in parser
        }.mapCatching(
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
}
