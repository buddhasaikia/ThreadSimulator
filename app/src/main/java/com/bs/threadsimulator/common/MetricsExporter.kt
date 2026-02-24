package com.bs.threadsimulator.common

import android.content.Context
import com.bs.threadsimulator.model.ExportedMetrics
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Data classes for JSON serialization.
 */
@Serializable
data class ExportedThreadMetric(
    val threadId: Long,
    val threadName: String,
    val updateType: String,
    val updateCount: Long,
    val avgUpdateTimeMs: Long,
)

@Serializable
data class MetricsExportJson(
    val exportTime: String,
    val metricsCount: Int,
    val metrics: List<ExportedThreadMetric>,
)

/**
 * Utility for exporting thread metrics to CSV and JSON formats.
 *
 * Handles serialization and file I/O for metrics snapshots, storing files
 * in the app's cache directory (no external permissions required).
 * Uses kotlinx.serialization for JSON to ensure proper escaping of special characters.
 */
class MetricsExporter
    @Inject
    constructor(
        private val context: Context,
    ) {
        private val json = Json { prettyPrint = true }

        /**
         * Exports metrics to CSV format.
         *
         * @param metrics List of ThreadMetrics to export
         * @return ExportedMetrics containing file path and export status
         */
        fun exportToCSV(metrics: List<ThreadMetrics>): ExportedMetrics {
            return try {
                if (metrics.isEmpty()) {
                    return ExportedMetrics.Error("No metrics to export")
                }

                val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
                val fileName = "metrics_$timestamp.csv"
                val file = File(context.cacheDir, fileName)

                val csvContent = buildCSVContent(metrics)
                file.writeText(csvContent)

                Timber.i("Metrics exported to CSV: %s", file.absolutePath)
                ExportedMetrics.Success(fileName, file.absolutePath, "csv")
            } catch (e: Exception) {
                Timber.e(e, "CSV export failed: %s", e.message)
                ExportedMetrics.Error(e.message ?: "CSV export failed")
            }
        }

        /**
         * Exports metrics to JSON format.
         *
         * Uses kotlinx.serialization for proper handling of special characters
         * and standards-compliant JSON generation.
         *
         * @param metrics List of ThreadMetrics to export
         * @return ExportedMetrics containing file path and export status
         */
        fun exportToJSON(metrics: List<ThreadMetrics>): ExportedMetrics {
            return try {
                if (metrics.isEmpty()) {
                    return ExportedMetrics.Error("No metrics to export")
                }

                val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
                val fileName = "metrics_$timestamp.json"
                val file = File(context.cacheDir, fileName)

                val jsonContent = buildJSONContent(metrics)
                file.writeText(jsonContent)

                Timber.i("Metrics exported to JSON: %s", file.absolutePath)
                ExportedMetrics.Success(fileName, file.absolutePath, "json")
            } catch (e: Exception) {
                Timber.e(e, "JSON export failed: %s", e.message)
                ExportedMetrics.Error(e.message ?: "JSON export failed")
            }
        }

        private fun buildCSVContent(metrics: List<ThreadMetrics>): String {
            val header =
                listOf(
                    "Thread ID",
                    "Thread Name",
                    "Update Type",
                    "Update Count",
                    "Avg Update Time (ms)",
                ).joinToString(",") { escapeCsv(it) }
            val rows =
                metrics.joinToString("\n") { metric ->
                    listOf(
                        metric.threadId.toString(),
                        metric.threadName,
                        metric.updateType,
                        metric.updateCount.toString(),
                        metric.avgUpdateTimeMs.toString(),
                    ).joinToString(",") { value -> escapeCsv(value) }
                }
            return header + "\n" + rows
        }

        /**
         * Escapes a single CSV field according to RFC 4180:
         * - Wraps the field in double quotes.
         * - Doubles any embedded double quotes.
         */
        private fun escapeCsv(value: String): String {
            val escaped = value.replace("\"", "\"\"")
            return "\"$escaped\""
        }

        private fun buildJSONContent(metrics: List<ThreadMetrics>): String {
            val exportTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
            val exportedMetrics =
                metrics.map { metric ->
                    ExportedThreadMetric(
                        threadId = metric.threadId,
                        threadName = metric.threadName,
                        updateType = metric.updateType,
                        updateCount = metric.updateCount,
                        avgUpdateTimeMs = metric.avgUpdateTimeMs,
                    )
                }
            val metricsExport =
                MetricsExportJson(
                    exportTime = exportTime,
                    metricsCount = metrics.size,
                    metrics = exportedMetrics,
                )
            return json.encodeToString(metricsExport)
        }
    }
