package com.wire.integrations.hold.exports.utils

import com.wire.integrations.hold.exports.dto.ExportResult
import org.junit.jupiter.api.Test
import pw.forst.tools.katlib.createJson
import pw.forst.tools.katlib.parseJson
import java.util.UUID
import kotlin.test.assertEquals


class ExportResultSerializationTest {
    @Test
    fun `test deserialization`() {
        runTest(ExportResult.Failure(UUID.randomUUID(), "no good"))
        runTest(ExportResult.Success(UUID.randomUUID()))
    }

    private fun runTest(expected: ExportResult) {
        val json = createJson(expected)
        val parsed = parseJson<ExportResult>(json)
        assertEquals(expected, parsed)
    }
}
