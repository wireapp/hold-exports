package com.wire.integrations.hold.exports.dao

import com.wire.integrations.hold.exports.dto.RawEvent
import mu.KLogging
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import pw.forst.tools.katlib.TimeProvider
import java.time.Instant
import java.util.UUID

class RawEventsRepository(
    private val nowProvider: TimeProvider<Instant>,
    private val batchSize: Int
) {

    private companion object : KLogging()

    /**
     * Returns all items waiting for the export.
     */
    fun getNotExportedEvents(): List<RawEvent> = transaction {
        logger.debug { "Loading events waiting for export." }

        Events.select { Events.exportedTime.isNull() }
            .limit(batchSize)
            .map { mapEvent(it) }
            .also { logger.debug { "${it.size} events loaded." } }
    }

    /**
     * Marks the given messages as exported.
     */
    fun markExported(messages: Set<UUID>) = transaction {
        val now = nowProvider.now()
        Events.update(
            where = { Events.messageId.inList(messages) },
            body = { it[exportedTime] = now }
        )
    }

    private fun mapEvent(it: ResultRow) = RawEvent(
        messageId = it[Events.messageId],
        conversationId = it[Events.conversationId],
        type = it[Events.type],
        payload = it[Events.payload],
        time = it[Events.time]
    )
}
