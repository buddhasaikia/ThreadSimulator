package com.bs.threadsimulator.common

import android.content.Context
import com.bs.threadsimulator.model.ExportedMetrics
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Utility for exporting thread metrics to CSV and JSON formats.
 *
 * Handles serialization and file I/O for metrics snapshots, storing files
 * in the app's cache directory (no external permissions required).
 */
class MetricsExporter
    @Inject
    constructor(
        private val context: Context,
    ) {
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
            val metricsJson =
                metrics.joinToString(",\n    ") { metric ->
                    """
                    {
                      "threadId": ${metric.threadId},
                      "threadName": ${escapeJson(metric.threadName)},
                      "updateType": ${escapeJson(metric.updateType)},
                      "updateCount": ${metric.updateCount},
                      "avgUpdateTimeMs": ${metric.avgUpdateTimeMs}
                    }
                    """.trimIndent()
                }
            val result =
                """
                {
                  "exportTime": ${escapeJson(exportTime)},
                  "metricsCount": ${metrics.size},
                  "metrics": [
                    $metricsJson
                  ]
                }
                """.trimIndent()
            return result
        }

        /**
         * Escapes a string for JSON according to RFC 7159.
         * Escapes control characters, quotes, and backslashes, then wraps in quotes.
         */
        private fun escapeJson(value: String): String {
            val escaped =
                value
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\u000C", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
            return "\"$escaped\""
        }
    }
