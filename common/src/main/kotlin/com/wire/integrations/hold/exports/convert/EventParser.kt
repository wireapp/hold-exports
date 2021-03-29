package com.wire.integrations.hold.exports.convert

import com.fasterxml.jackson.databind.ObjectMapper
import com.wire.integrations.hold.exports.dto.ConversationEvent
import com.wire.integrations.hold.exports.dto.RawEvent
import mu.KLogging

/**
 * Provides ability to parse [RawEvent.payload] to [ConversationEvent].
 */
class EventParser(
    private val mapper: ObjectMapper
) {

    internal companion object : KLogging() {
        val events = mapOf(
            "conversation.create" to ConversationEvent.SystemEvent.Create::class,
            "conversation.member-join" to ConversationEvent.SystemEvent.MemberJoin::class,
            "conversation.member-leave" to ConversationEvent.SystemEvent.MemberLeave::class,
            // events
            "conversation.otr-message-add.delete-text" to ConversationEvent.OtrEvent.DeleteText::class,
            "conversation.otr-message-add.reaction" to ConversationEvent.OtrEvent.Reaction::class,
            // text
            "conversation.otr-message-add.new-text" to ConversationEvent.OtrEvent.Text.NewText::class,
            "conversation.otr-message-add.edit-text" to ConversationEvent.OtrEvent.Text.EditText::class,
            // assets
            "conversation.otr-message-add.new-image" to ConversationEvent.OtrEvent.Asset.NewImage::class,
            "conversation.otr-message-add.new-attachment" to ConversationEvent.OtrEvent.Asset.NamedAsset.NewAttachment::class,
            "conversation.otr-message-add.new-audio" to ConversationEvent.OtrEvent.Asset.NamedAsset.NewAudio::class,
            "conversation.otr-message-add.new-video" to ConversationEvent.OtrEvent.Asset.NamedAsset.NewVideo::class
        )
    }

    /**
     * Parses collection of [events] and returns those that was parsed without errors.
     */
    fun parse(events: Iterable<RawEvent>): List<ConversationEvent> = events.mapNotNull { parse(it) }

    /**
     * Parse single [RawEvent], returns null if something goes wrong.
     */
    fun parse(event: RawEvent): ConversationEvent? = runCatching { parseEventUnsafe(event) }
        .onFailure {
            logger.warn { "Could not parse event ${it.message}. Whole event:\n${event}" }
        }.getOrNull()

    private fun parseEventUnsafe(event: RawEvent): ConversationEvent = events[event.type]
        ?.let { mapper.readValue(event.payload, it.java) }
        ?: throw IllegalArgumentException("Unknown event type: ${event.type}.")
}
