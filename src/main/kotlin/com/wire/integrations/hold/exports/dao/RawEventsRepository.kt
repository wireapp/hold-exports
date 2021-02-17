package com.wire.integrations.hold.exports.dao

import com.wire.integrations.hold.exports.utils.mapCatching
import mu.KLogging
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import pw.forst.tools.katlib.jacksonMapper
import pw.forst.tools.katlib.toUuid

class RawEventsRepository {
    private companion object : KLogging()

    /**
     * Returns all items waiting for the export.
     */
    fun getAllUnexportedRawEvents(): List<RawEventDto> = transaction {
        logger.debug { "Loading events waiting for export." }
        val mapper = jacksonMapper()
        Events.select { Events.exportedTime.isNull() }
            .mapCatching({
                RawEventDto(
                    messageId = it[Events.messageId].toUuid(),
                    conversationId = it[Events.conversationId].toUuid(),
                    type = it[Events.type],
                    payload = mapper.readTree(it[Events.payload]),
                    time = it[Events.time]
                )
            }, { "It was not possible to map record:\n$it" })
            .also { logger.debug { "${it.size} events loaded." } }
    }

}
