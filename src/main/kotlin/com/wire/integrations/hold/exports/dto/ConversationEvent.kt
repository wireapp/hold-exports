package com.wire.integrations.hold.exports.dto

import java.time.Instant
import java.util.UUID

/**
 * This is quite ugly, but very efficient representation of all messages.
 */
@Suppress("ArrayInDataClass") // native implementations, we won't be comparing them
sealed class ConversationEvent {
    abstract val id: UUID
    abstract val convId: UUID
    abstract val type: String

    data class Create(
        override val id: UUID,
        override val convId: UUID,
        override val type: String = "conversation.create",
        val from: UUID,
        val time: Instant
    ) : ConversationEvent()

    data class MemberJoin(
        override val id: UUID,
        override val convId: UUID,
        override val type: String = "conversation.member-join",
        val conversation: Conversation,
        val from: UUID,
        val time: Instant,
        val users: List<UUID>
    ) : ConversationEvent()

    data class MemberLeave(
        override val id: UUID,
        override val convId: UUID,
        override val type: String = "conversation.member-leave",
        val conversation: Conversation,
        val from: UUID,
        val time: Instant,
        val users: List<UUID>
    ) : ConversationEvent()

    data class Conversation(
        val id: UUID
    )

    sealed class OtrEvent : ConversationEvent() {
        abstract val messageId: UUID
        abstract val conversationId: UUID
        abstract val userId: UUID
        abstract val clientId: String
        abstract val time: Instant

        override val id: UUID
            get() = messageId

        override val convId: UUID
            get() = conversationId

        data class DeleteText(
            override val messageId: UUID,
            override val conversationId: UUID,
            override val userId: UUID,
            override val clientId: String,
            override val time: Instant,
            override val type: String = "conversation.otr-message-add.delete-text",
            val deletedMessageId: UUID,
        ) : OtrEvent()

        data class Reaction(
            override val messageId: UUID,
            override val conversationId: UUID,
            override val userId: UUID,
            override val clientId: String,
            override val time: Instant,
            override val type: String = "conversation.otr-message-add.reaction",
            val emoji: String,
            val reactionMessageId: UUID
        ) : OtrEvent()

        sealed class Text : OtrEvent() {
            abstract val text: String
            abstract val mentions: List<Mention>

            data class NewText(
                override val messageId: UUID,
                override val conversationId: UUID,
                override val userId: UUID,
                override val clientId: String,
                override val time: Instant,
                override val type: String = "conversation.otr-message-add.new-text",
                override val text: String,
                override val mentions: List<Mention>
            ) : Text()

            data class EditText(
                override val messageId: UUID,
                override val conversationId: UUID,
                override val userId: UUID,
                override val clientId: String,
                override val time: Instant,
                override val type: String = "conversation.otr-message-add.edit-text",
                override val text: String,
                override val mentions: List<Mention>,
                val replacingMessageId: UUID
            ) : Text()

            data class Mention(
                val userId: UUID,
                val offset: Int,
                val length: Int
            )
        }

        sealed class Asset : OtrEvent() {
            abstract val assetKey: String
            abstract val assetToken: String?
            abstract val mimeType: String
            abstract val otrKey: ByteArray
            abstract val sha256: ByteArray
            abstract val size: Long

            sealed class NamedAsset : Asset() {
                abstract val name: String

                data class NewAttachment(
                    override val messageId: UUID,
                    override val conversationId: UUID,
                    override val userId: UUID,
                    override val clientId: String,
                    override val time: Instant,
                    override val type: String = "conversation.otr-message-add.new-attachment",
                    override val assetKey: String,
                    override val mimeType: String,
                    override val otrKey: ByteArray,
                    override val sha256: ByteArray,
                    override val size: Long,
                    override val assetToken: String?,
                    override val name: String,
                ) : NamedAsset()

                data class NewAudio(
                    override val messageId: UUID,
                    override val conversationId: UUID,
                    override val userId: UUID,
                    override val clientId: String,
                    override val time: Instant,
                    override val type: String = "conversation.otr-message-add.new-audio",
                    override val assetKey: String,
                    override val assetToken: String,
                    override val mimeType: String,
                    override val otrKey: ByteArray,
                    override val sha256: ByteArray,
                    override val size: Long,
                    override val name: String,
                    val duration: Long,
                ) : NamedAsset()

                data class NewVideo(
                    override val messageId: UUID,
                    override val conversationId: UUID,
                    override val userId: UUID,
                    override val clientId: String,
                    override val time: Instant,
                    override val type: String = "conversation.otr-message-add.new-video",
                    override val assetKey: String,
                    override val mimeType: String,
                    override val otrKey: ByteArray,
                    override val sha256: ByteArray,
                    override val size: Long,
                    override val assetToken: String?,
                    override val name: String,
                    val duration: Int,
                    val height: Int,
                    val width: Int,
                ) : NamedAsset()
            }

            data class NewImage(
                override val messageId: UUID,
                override val conversationId: UUID,
                override val userId: UUID,
                override val clientId: String,
                override val time: Instant,
                override val type: String = "conversation.otr-message-add.new-image",
                override val assetKey: String,
                override val mimeType: String,
                override val otrKey: ByteArray,
                override val sha256: ByteArray,
                override val size: Long,
                override val assetToken: String?,
                val height: Int,
                val width: Int,
            ) : Asset()
        }
    }
}
