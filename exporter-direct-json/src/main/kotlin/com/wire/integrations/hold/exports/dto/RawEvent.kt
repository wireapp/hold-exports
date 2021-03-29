package com.wire.integrations.hold.exports.dto

import java.time.Instant
import java.util.UUID

/**
 * Unprocessed event from the Legal Hold database.
 */
data class RawEvent(
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
    val payload: String,
    /**
     * Time when the message was put to the database.
     */
    val time: Instant
)
