package com.bs.threadsimulator.common

import android.content.Context
import com.bs.threadsimulator.model.ExportedMetrics
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class MetricsExporterTest {
    private lateinit var context: Context
    private lateinit var exporter: MetricsExporter
    private lateinit var testDir: File

    @Before
    fun setup() {
        testDir = File.createTempFile("test", "").parentFile!!
        context = mockk()
        every { context.cacheDir } returns testDir
        exporter = MetricsExporter(context)
    }

    @Test
    fun exportToCSV_WithValidMetrics_ReturnsSuccess() {
        val metrics =
            listOf(
                ThreadMetrics(1L, "Thread-1", "PE", 100, 50),
                ThreadMetrics(2L, "Thread-2", "CurrentPrice", 200, 75),
            )

        val result = exporter.exportToCSV(metrics)

        assertTrue(result is ExportedMetrics.Success)
        val success = result as ExportedMetrics.Success
        assertEquals("csv", success.format)
        assertTrue(success.fileName.startsWith("metrics_"))
        assertTrue(success.fileName.endsWith(".csv"))
        assertTrue(File(success.filePath).exists())
    }

    @Test
    fun exportToCSV_WithEmptyMetrics_ReturnsError() {
        val result = exporter.exportToCSV(emptyList())

        assertTrue(result is ExportedMetrics.Error)
        val error = result as ExportedMetrics.Error
        assertEquals("No metrics to export", error.message)
    }

    @Test
    fun exportToCSV_WithSpecialCharacters_ProperlyEscapes() {
        val metrics =
            listOf(
                ThreadMetrics(1L, "Thread,With\"Quote", "Type\nWithNewline", 10, 5),
            )

        val result = exporter.exportToCSV(metrics)

        assertTrue(result is ExportedMetrics.Success)
        val success = result as ExportedMetrics.Success
        val csvFile = File(success.filePath)
        val content = csvFile.readText()

        // CSV should have escaped quotes
        assertTrue(content.contains("\"Thread,With\"\"Quote\""))
        assertTrue(content.contains("\"Type\nWithNewline\""))
    }

    @Test
    fun exportToCSV_FileContainsHeaders() {
        val metrics = listOf(ThreadMetrics(1L, "Thread-1", "PE", 50, 25))

        val result = exporter.exportToCSV(metrics)

        assertTrue(result is ExportedMetrics.Success)
        val success = result as ExportedMetrics.Success
        val csvFile = File(success.filePath)
        val content = csvFile.readText()

        val lines = content.split("\n")
        assertTrue(lines.size >= 2) // Header + at least 1 data line
        assertTrue(lines[0].contains("Thread ID"))
        assertTrue(lines[0].contains("Thread Name"))
        assertTrue(lines[0].contains("Update Type"))
    }

    @Test
    fun exportToJSON_WithValidMetrics_ReturnsSuccess() {
        val metrics =
            listOf(
                ThreadMetrics(1L, "Thread-1", "PE", 100, 50),
                ThreadMetrics(2L, "Thread-2", "CurrentPrice", 200, 75),
            )

        val result = exporter.exportToJSON(metrics)

        assertTrue(result is ExportedMetrics.Success)
        val success = result as ExportedMetrics.Success
        assertEquals("json", success.format)
        assertTrue(success.fileName.startsWith("metrics_"))
        assertTrue(success.fileName.endsWith(".json"))
        assertTrue(File(success.filePath).exists())
    }

    @Test
    fun exportToJSON_WithEmptyMetrics_ReturnsError() {
        val result = exporter.exportToJSON(emptyList())

        assertTrue(result is ExportedMetrics.Error)
        val error = result as ExportedMetrics.Error
        assertEquals("No metrics to export", error.message)
    }

    @Test
    fun exportToJSON_WithSpecialCharacters_ProperlyEscapes() {
        val metrics =
            listOf(
                ThreadMetrics(
                    1L,
                    "Thread\"With\\Backslash",
                    "Type\nWith\tControl\rChars",
                    10,
                    5,
                ),
            )

        val result = exporter.exportToJSON(metrics)

        assertTrue(result is ExportedMetrics.Success)
        val success = result as ExportedMetrics.Success
        val jsonFile = File(success.filePath)
        val content = jsonFile.readText()

        // JSON should have properly escaped special characters
        assertTrue(content.contains("\"threadName\": \"Thread\\\"With\\\\Backslash\""))
        assertTrue(content.contains("\\n"))
        assertTrue(content.contains("\\t"))
        assertTrue(content.contains("\\r"))
    }

    @Test
    fun exportToJSON_FileContainsMetadata() {
        val metrics = listOf(ThreadMetrics(1L, "Thread-1", "PE", 50, 25))

        val result = exporter.exportToJSON(metrics)

        assertTrue(result is ExportedMetrics.Success)
        val success = result as ExportedMetrics.Success
        val jsonFile = File(success.filePath)
        val content = jsonFile.readText()

        assertTrue(content.contains("\"exportTime\""))
        assertTrue(content.contains("\"metricsCount\": 1"))
        assertTrue(content.contains("\"metrics\""))
    }

    @Test
    fun exportToJSON_ValidJsonStructure() {
        val metrics = listOf(ThreadMetrics(1L, "Thread-1", "PE", 50, 25))

        val result = exporter.exportToJSON(metrics)

        assertTrue(result is ExportedMetrics.Success)
        val success = result as ExportedMetrics.Success
        val jsonFile = File(success.filePath)
        val content = jsonFile.readText().trim()

        // Basic JSON structure validation
        assertTrue(content.startsWith("{"))
        assertTrue(content.endsWith("}"))
        assertTrue(content.contains("\"metrics\""))
    }

    @Test
    fun exportToCSV_FileCreatedInCacheDir() {
        val metrics = listOf(ThreadMetrics(1L, "Thread-1", "PE", 10, 5))

        val result = exporter.exportToCSV(metrics)

        assertTrue(result is ExportedMetrics.Success)
        val success = result as ExportedMetrics.Success

        // Verify file is in cache directory
        assertTrue(success.filePath.startsWith(testDir.absolutePath))
    }

    @Test
    fun exportToJSON_FileCreatedInCacheDir() {
        val metrics = listOf(ThreadMetrics(1L, "Thread-1", "PE", 10, 5))

        val result = exporter.exportToJSON(metrics)

        assertTrue(result is ExportedMetrics.Success)
        val success = result as ExportedMetrics.Success

        // Verify file is in cache directory
        assertTrue(success.filePath.startsWith(testDir.absolutePath))
    }
}
