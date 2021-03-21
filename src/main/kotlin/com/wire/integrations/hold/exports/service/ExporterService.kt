package com.wire.integrations.hold.exports.service

import com.wire.integrations.hold.exports.dao.RawEventsRepository
import com.wire.integrations.hold.exports.dto.EnrichedEvent
import com.wire.integrations.hold.exports.export.Exporter
import com.wire.integrations.hold.exports.utils.mapCatching
import mu.KLogging
import pw.forst.tools.katlib.mapToSet

class ExporterService(
    private val rawEventsRepository: RawEventsRepository,
    private val processingService: ProcessingService,
    private val exporter: Exporter
) {

    private companion object : KLogging()

    /**
     * Executes whole pipeline of loading, exporting and marking events.
     */
    fun executeExports() {
        logger.debug { "Loading events to export." }
        val toExport = processingService.loadAndProcess()

        logger.debug { "Exporting ${toExport.size} events." }
        val exported = exporter.export(toExport)

        logger.debug { "Exported ${exported.size} events." }
        if (exported.size != toExport.size) {
            logger.warn { "Some events were not exported." }
            val missing = toExport.mapToSet { it.rawEvent.messageId }.subtract(exported)
                .joinToString(", ")
            logger.warn { "Events that were not exported:\n$missing" }
        }

        logger.debug { "Saving exported events." }
        rawEventsRepository.markExported(exported)
        logger.debug { "Exported events saved." }

        val toClean = toExport
            .filterIsInstance<EnrichedEvent.AssetEvent>()
            .map { it.savedFile }
        logger.debug { "Cleaning up, deleting ${toClean.size} files." }

        val filesDeleted = toClean.mapCatching(
            transform = { file -> file.delete().takeIf { it }?.let { file.absolutePath } },
            errorLog = { "It was not possible to delete file: ${it.absolutePath}." }
        )
        logger.debug { "Deleted ${filesDeleted.size} temporary files." }
        if (filesDeleted.size != toClean.size) {
            logger.warn { "${toClean.size - filesDeleted.size} files were not deleted!" }
            val missing = toClean.mapToSet { it.absolutePath }.subtract(filesDeleted)
                .joinToString("\n")
            logger.warn { "See following list:\n${missing}" }
        }
        logger.debug { "Task finished." }
    }
}
