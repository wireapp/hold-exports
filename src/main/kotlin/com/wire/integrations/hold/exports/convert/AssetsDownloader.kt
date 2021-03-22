package com.wire.integrations.hold.exports.convert

import com.wire.helium.API
import com.wire.integrations.hold.exports.dto.ConversationEvent
import com.wire.xenon.tools.Util
import mu.KLogging
import java.security.MessageDigest

class AssetsDownloader(private val apiProvider: () -> API) {

    private lateinit var api: API

    private companion object : KLogging()

    /**
     * Downloads asset from Wire servers and decrypts it.
     *
     * Returns byte array with the decrypted data.
     * Throws Exception if download, check or decryption failed.
     */
    fun downloadAndDecrypt(asset: ConversationEvent.OtrEvent.Asset): ByteArray {
        val cipher = download(asset)
        val sha256 = MessageDigest.getInstance("SHA-256").digest(cipher)

        require(sha256.contentEquals(asset.sha256)) { "Failed sha256 check." }

        return Util.decrypt(asset.otrKey, cipher)
    }

    // allow just a single download at one time
    // as we might or might not change the API instance
    // TODO this might be bottleneck in the future
    @Synchronized
    private fun download(asset: ConversationEvent.OtrEvent.Asset): ByteArray {
        api = if (::api.isInitialized) api else apiProvider()
        return runCatching {
            // try to download the asset with current login
            api.downloadAsset(asset.assetKey, asset.assetToken)
        }.onFailure {
            logger.warn { "Download failed with message ${it.message}. Trying to log in and download again." }
        }.recoverCatching {
            // if the previous request failed, try to login and download the asset again
            api = apiProvider()
            api.downloadAsset(asset.assetKey, asset.assetToken)
        }.onFailure {
            logger.error { "Download failed for second time, throwing. ${it.message}." }
        }.onSuccess {
            logger.debug { "Downloaded asset ${asset.messageId}." }
        }.getOrThrow()
    }
}
