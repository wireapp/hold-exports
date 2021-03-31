package com.wire.integrations.hold.exports

import com.wire.integrations.hold.exports.asset.AssetsFileDownloader
import com.wire.integrations.hold.exports.convert.EventParser
import com.wire.integrations.hold.exports.dto.ConversationEvent
import com.wire.integrations.hold.exports.dto.EnrichedEvent
import com.wire.integrations.hold.exports.dto.RawEvent

/**
 * Service that converts [RawEvent] to [EnrichedEvent].
 */
class EventEnricher(
    private val parser: EventParser,
    private val assetsFileDownloader: AssetsFileDownloader
) {
    /**
     * Parses event and downloads assets if necessary.
     *
     * Throws [Exception] if something goes south.
     */
    fun enrich(rawEvent: RawEvent): EnrichedEvent {
        // parse event
        val conversationEvent = parser.parse(rawEvent)
            ?: throw IllegalArgumentException("It was not possible to parse data for message ${rawEvent.messageId}.")
        // enrich event
        return when (conversationEvent) {
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
    }

}
