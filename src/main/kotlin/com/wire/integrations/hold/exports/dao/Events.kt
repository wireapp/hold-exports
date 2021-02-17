package com.wire.integrations.hold.exports.dao

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime

object Events : Table("events") {
    val messageId: Column<String> = varchar("messageId", 36)
    val conversationId: Column<String> = varchar("conversationId", 36)
    val type: Column<String> = varchar("type", 65_535)
    val payload: Column<String> = varchar("payload", 65_535)
    val time: Column<LocalDateTime> = datetime("time")
    val exportedTime: Column<LocalDateTime?> = datetime("exported").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(messageId)
}
