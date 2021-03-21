package com.wire.integrations.hold.exports.export

import com.wire.integrations.hold.exports.dto.ConversationEvent
import com.wire.integrations.hold.exports.dto.EnrichedEvent
import mu.KLogging
import pw.forst.tools.katlib.mapToSet
import java.util.UUID

/**
 * Implementation of [Exporter] that simply prints data to console.
 *
 * Note that this should not be in the production.
 */
class ConsoleLogExporter : Exporter {

    private companion object : KLogging()

    private fun print(string: String) = logger.info {
        """
            Printing data:
            --------------
            $string
            ______________
        """.trimIndent()
    }

    override fun export(events: Iterable<EnrichedEvent>): Set<UUID> {
        val stringToPrint = events.groupBy { it.rawEvent.conversationId }
            .map { (conversationId, events) ->
                val conversationStrings = events
                    .sortedBy { it.rawEvent.time }
                    .joinToString("\n") { buildStringForEvent(it) }
                val conv = "Conversation: $conversationId"
                val hashes = (0..conv.length).joinToString("") { "#" }
                "${hashes}${conv}${hashes}${conversationStrings}"
            }.joinToString("\n")

        print(stringToPrint)
        return events.mapToSet { it.rawEvent.messageId }
    }

    private fun buildStringForEvent(event: EnrichedEvent): String {
        val prefix = "${event.rawEvent.time}:"
        val value = when (val e = event.conversationEvent) {
            is ConversationEvent.OtrEvent.Asset -> {
                val file = (event as EnrichedEvent.AssetEvent).savedFile.absoluteFile
                val attachmentType = when (e) {
                    is ConversationEvent.OtrEvent.Asset.NamedAsset.NewAttachment -> "attachment \"${e.name}\""
                    is ConversationEvent.OtrEvent.Asset.NamedAsset.NewAudio -> "audio recording"
                    is ConversationEvent.OtrEvent.Asset.NamedAsset.NewVideo -> "video"
                    is ConversationEvent.OtrEvent.Asset.NewImage -> "image"
                }
                "${e.userId} sent $attachmentType - saved in \"$file\"."
            }
            is ConversationEvent.OtrEvent.DeleteText -> {
                "${e.userId} deleted message ${e.deletedMessageId}."
            }
            is ConversationEvent.OtrEvent.Reaction -> {
                "${e.userId} sent reaction: ${e.emoji}."
            }
            is ConversationEvent.OtrEvent.Text.EditText -> {
                "${e.userId} edited message ${e.replacingMessageId} with new text: \"${e.text}\"."
            }
            is ConversationEvent.OtrEvent.Text.NewText -> {
                "${e.userId} set text: \"${e.text}\"."
            }
            is ConversationEvent.SystemEvent.Create -> {
                "Conversation ${e.convId} created."
            }
            is ConversationEvent.SystemEvent.MemberJoin -> {
                "Following users ${e.users.joinToString(", ")} joined the conversation ${e.conversation.id}."
            }
            is ConversationEvent.SystemEvent.MemberLeave -> {
                "Following users ${e.users.joinToString(", ")} left the conversation ${e.conversation.id}."
            }
        }
        return "$prefix $value"
    }
}