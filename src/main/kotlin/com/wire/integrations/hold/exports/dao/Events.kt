package com.wire.integrations.hold.exports.dao

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Events : Table("events") {
    val messageId = uuid("messageid")
    val conversationId = uuid("conversationid")
    val type = text("type")
    val payload = text("payload")
    val time = timestamp("time")
    val exportedTime = timestamp("exported").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(messageId)
}
