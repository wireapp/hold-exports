package com.wire.integrations.hold.exports.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.wire.integrations.hold.exports.utils.ExportResultDeserializer
import com.wire.integrations.hold.exports.utils.ExportResultSerializer
import com.wire.integrations.hold.exports.utils.MessageId

/**
 * Result of the message export.
 */
@JsonDeserialize(using = ExportResultDeserializer::class)
@JsonSerialize(using = ExportResultSerializer::class)
sealed class ExportResult {
    /**
     * Wire message ID.
     */
    abstract val messageId: MessageId

    /**
     * Success result.
     */
    data class Success(
        override val messageId: MessageId
    ) : ExportResult()

    /**
     * Export failed.
     */
    data class Failure(
        override val messageId: MessageId,
        val errorMessage: String
    ) : ExportResult()
}
