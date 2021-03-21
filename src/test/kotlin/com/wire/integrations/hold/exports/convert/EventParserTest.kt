package com.wire.integrations.hold.exports.convert

import com.wire.integrations.hold.exports.utils.DiAwareTest
import com.wire.integrations.hold.exports.utils.prototypeEvent
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class EventParserTest : DiAwareTest() {

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
