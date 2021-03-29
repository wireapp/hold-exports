package com.wire.integrations.hold.exports.convert

import com.wire.integrations.hold.exports.dto.ConversationEvent
import com.wire.integrations.hold.exports.utils.DiAwareTest
import com.wire.integrations.hold.exports.utils.getAllEvents
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.kodein.di.instance

class AssetsFileDownloaderTest : DiAwareTest() {

    @Test
    @Disabled("integration test, needs wire credentials to staging")
    fun `test download asset`() {
        val parser by di.instance<EventParser>()
        val assetDownloader by di.instance<AssetsFileDownloader>()

        val assetFiles = getAllEvents(parser)
            .filterIsInstance<ConversationEvent.OtrEvent.Asset>()
            .map { asset ->
                assetDownloader.downloadAndSave(asset).also {
                    println(it.absolutePath)
                }
            }
        println(assetFiles.size)
    }

}
