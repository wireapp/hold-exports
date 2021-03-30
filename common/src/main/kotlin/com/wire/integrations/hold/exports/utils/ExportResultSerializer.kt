package com.wire.integrations.hold.exports.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.wire.integrations.hold.exports.dto.ExportResult
import pw.forst.tools.katlib.toUuid

const val success = "success"

class ExportResultSerializer : JsonSerializer<ExportResult>() {
    override fun serialize(value: ExportResult, jgen: JsonGenerator, provider: SerializerProvider) {
        with(jgen) {
            writeStartObject()
            writeStringField(value::messageId.name, value.messageId.toString())
            val wasSuccess = when (value) {
                is ExportResult.Failure -> false.also { writeStringField(value::errorMessage.name, value.errorMessage) }
                is ExportResult.Success -> true
            }
            writeBooleanField(success, wasSuccess)
            writeEndObject()
        }
    }
}

class ExportResultDeserializer : JsonDeserializer<ExportResult>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ExportResult {
        val node = p.readValueAsTree<JsonNode>()
        val success = node.get(success).asBoolean()
        val messageId = node.get(ExportResult::messageId.name).asText().toUuid()
        return if (success) {
            ExportResult.Success(messageId)
        } else {
            ExportResult.Failure(messageId, node.get(ExportResult.Failure::errorMessage.name).asText())
        }
    }
}
