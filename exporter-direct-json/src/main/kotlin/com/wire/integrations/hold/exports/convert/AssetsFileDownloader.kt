package com.wire.integrations.hold.exports.convert

import com.wire.integrations.hold.exports.dto.ConversationEvent
import java.io.File

class AssetsFileDownloader(private val downloader: AssetsDownloader) {

    /**
     * Downloads file and saves it in the file.
     *
     * Throws various exceptions if download or decryption failed. See [AssetsDownloader.downloadAndDecrypt].
     */
    fun downloadAndSave(asset: ConversationEvent.OtrEvent.Asset): File {
        val bytes = downloader.downloadAndDecrypt(asset)
        val fileFactory: (String) -> File = { File.createTempFile("wire_", "_${asset.messageId}_${it}") }
        return when (asset) {
            is ConversationEvent.OtrEvent.Asset.NamedAsset -> fileFactory(asset.name)
            is ConversationEvent.OtrEvent.Asset.NewImage -> fileFactory("image.${determineExtension(asset.mimeType)}")
        }.also { it.writeBytes(bytes) }
    }

    private fun determineExtension(mimeType: String) = mimeType
        .split("/")
        .let { if (it.size == 1) it[0] else it[1] }
}
