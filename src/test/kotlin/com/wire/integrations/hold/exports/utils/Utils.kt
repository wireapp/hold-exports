package com.wire.integrations.hold.exports.utils

import com.wire.integrations.hold.exports.convert.EventParser
import com.wire.integrations.hold.exports.dto.ConversationEvent
import com.wire.integrations.hold.exports.dto.RawEvent
import java.time.Instant
import java.util.UUID

val prototypeEvent = RawEvent(
    messageId = UUID.randomUUID(),
    conversationId = UUID.randomUUID(),
    type = "",
    payload = "",
    time = Instant.EPOCH
)

fun getAllEvents(parser: EventParser): List<ConversationEvent> =
    EventParser.events.keys.mapNotNull { eventType ->
        val payload = parser.javaClass.getResource("/events/$eventType.json").readText()
        parser.parse(prototypeEvent.copy(type = eventType, payload = payload))
    }
