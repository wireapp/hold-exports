package com.wire.integrations.hold.exports.dao

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.UUID

/**
 * Unprocessed event from the Legal Hold database.
 */
data class RawEventDto(
    /**
     * ID of the message in the Wire.
     */
    val messageId: UUID,
    /**
     * ID of the conversation in the Wire.
     */
    val conversationId: UUID,
    /**
     * Type of the message.
     *
     * Examples: "conversation.create", "conversation.create", "conversation.otr-message-add.new-image"
     */
    val type: String,
    /**
     * The message itself as JSON.
     */
    val payload: JsonNode,
    /**
     * Time when the message was put to the database.
     */
    val time: LocalDateTime
)
