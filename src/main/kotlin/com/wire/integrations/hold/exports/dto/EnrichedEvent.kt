package com.wire.integrations.hold.exports.dto

import java.io.File

sealed class EnrichedEvent {
    abstract val rawEvent: RawEvent
    abstract val conversationEvent: ConversationEvent

    data class SystemEvent(
        override val rawEvent: RawEvent,
        override val conversationEvent: ConversationEvent.SystemEvent
    ) : EnrichedEvent()

    data class TextEvent(
        override val rawEvent: RawEvent,
        override val conversationEvent: ConversationEvent.OtrEvent
    ) : EnrichedEvent()

    data class AssetEvent(
        override val rawEvent: RawEvent,
        override val conversationEvent: ConversationEvent.OtrEvent.Asset,
        val savedFile: File
    ) : EnrichedEvent()
}
