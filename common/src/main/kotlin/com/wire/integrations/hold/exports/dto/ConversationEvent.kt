package com.wire.integrations.hold.exports.dto

import com.wire.integrations.hold.exports.utils.ConversationId
import com.wire.integrations.hold.exports.utils.MessageId
import com.wire.integrations.hold.exports.utils.UserId
import java.time.Instant

/**
 * This is quite ugly, but very efficient representation of all messages.
 */
@Suppress("ArrayInDataClass") // native implementations, we won't be comparing them
sealed class ConversationEvent {
    abstract val id: MessageId
    abstract val convId: ConversationId
    abstract val type: String

    sealed class SystemEvent : ConversationEvent() {
        abstract val time: Instant
        abstract val from: UserId
        abstract val conversation: Conversation?

        data class Create(
            override val id: MessageId,
            override val convId: ConversationId,
            override val type: String = "conversation.create",
            override val from: UserId,
            override val time: Instant,
            override val conversation: Conversation.Created?
        ) : SystemEvent()

        data class MemberJoin(
            override val id: MessageId,
            override val convId: ConversationId,
            override val type: String = "conversation.member-join",
            override val from: UserId,
            override val time: Instant,
            override val conversation: Conversation.Changed,
            val users: List<UserId>
        ) : SystemEvent()

        data class MemberLeave(
            override val id: MessageId,
            override val convId: ConversationId,
            override val type: String = "conversation.member-leave",
            override val from: UserId,
            override val time: Instant,
            override val conversation: Conversation.Changed,
            val users: List<UserId>
        ) : SystemEvent()

        sealed class Conversation {
            abstract val id: ConversationId

            data class Changed(
                override val id: ConversationId
            ) : Conversation()

            data class Created(
                override val id: ConversationId,
                val creator: UserId,
                val members: List<Member>,
                val name: String?
            ) : Conversation() {
                data class Member(
                    val id: UserId,
                    val status: Int?
                )
            }

        }
    }

    sealed class OtrEvent : ConversationEvent() {
        abstract val messageId: MessageId
        abstract val conversationId: ConversationId
        abstract val userId: UserId
        abstract val clientId: String
        abstract val time: Instant

        override val id
            get() = messageId

        override val convId
            get() = conversationId

        data class DeleteText(
            override val messageId: MessageId,
            override val conversationId: ConversationId,
            override val userId: UserId,
            override val clientId: String,
            override val time: Instant,
            override val type: String = "conversation.otr-message-add.delete-text",
            val deletedMessageId: MessageId,
        ) : OtrEvent()

        data class Reaction(
            override val messageId: MessageId,
            override val conversationId: ConversationId,
            override val userId: UserId,
            override val clientId: String,
            override val time: Instant,
            override val type: String = "conversation.otr-message-add.reaction",
            val emoji: String,
            val reactionMessageId: MessageId
        ) : OtrEvent()

        sealed class Text : OtrEvent() {
            abstract val text: String
            abstract val mentions: List<Mention>
            abstract val expireAfterMillis: Long?
            abstract val quotedMessageId: MessageId?

            data class NewText(
                override val messageId: MessageId,
                override val conversationId: ConversationId,
                override val userId: UserId,
                override val clientId: String,
                override val time: Instant,
                override val type: String = "conversation.otr-message-add.new-text",
                override val text: String,
                override val mentions: List<Mention>,
                override val expireAfterMillis: Long?,
                override val quotedMessageId: MessageId?,
            ) : Text()

            data class EditText(
                override val messageId: MessageId,
                override val conversationId: ConversationId,
                override val userId: UserId,
                override val clientId: String,
                override val time: Instant,
                override val type: String = "conversation.otr-message-add.edit-text",
                override val text: String,
                override val mentions: List<Mention>,
                override val expireAfterMillis: Long?,
                override val quotedMessageId: MessageId?,
                val replacingMessageId: MessageId,
            ) : Text()

            data class Mention(
                val userId: UserId,
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
                    override val messageId: MessageId,
                    override val conversationId: ConversationId,
                    override val userId: UserId,
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
                    override val messageId: MessageId,
                    override val conversationId: ConversationId,
                    override val userId: UserId,
                    override val clientId: String,
                    override val time: Instant,
                    override val type: String = "conversation.otr-message-add.new-audio",
                    override val assetKey: String,
                    override val assetToken: String?,
                    override val mimeType: String,
                    override val otrKey: ByteArray,
                    override val sha256: ByteArray,
                    override val size: Long,
                    override val name: String,
                    val duration: Long,
                ) : NamedAsset()

                data class NewVideo(
                    override val messageId: MessageId,
                    override val conversationId: ConversationId,
                    override val userId: UserId,
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
                override val messageId: MessageId,
                override val conversationId: ConversationId,
                override val userId: UserId,
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
