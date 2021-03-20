package com.wire.integrations.hold.exports.convert

import com.wire.integrations.hold.exports.di
import com.wire.integrations.hold.exports.dto.RawEvent
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class EventParserTest {

    private val prototypeEvent = RawEvent(
        messageId = UUID.randomUUID(),
        conversationId = UUID.randomUUID(),
        type = "",
        payload = "",
        time = Instant.EPOCH
    )

    @Test
    fun `test parse all messages`() {
        val parser by di.instance<EventParser>()

        val events = EventParser.events.keys

        events.map { eventType ->
            val payload = javaClass.getResource("/events/$eventType.json").readText()
            val result = parser.parse(prototypeEvent.copy(type = eventType, payload = payload))
            assertNotNull(result)
            assertEquals(eventType, result.type)
        }
    }

}
