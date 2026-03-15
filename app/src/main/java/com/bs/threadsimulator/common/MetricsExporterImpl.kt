package com.bs.threadsimulator.common

import android.content.Context
import com.bs.threadsimulator.model.ExportedMetrics
import com.bs.threadsimulator.model.MetricsExporter
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implementation of MetricsExporter for CSV and JSON export formats.
 *
 * Handles serialization of metrics to files in the app's cache directory.
 */
class MetricsExporterImpl(
    private val context: Context,
) : MetricsExporter {
    private val timeFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    override suspend fun exportToCSV(metrics: List<Map<String, Any>>): ExportedMetrics =
        try {
            val timestamp = timeFormat.format(Date())
            val fileName = "metrics_$timestamp.csv"
            val file = File(context.cacheDir, fileName)
            val csvContent = buildCSVContent(metrics)
            file.writeText(csvContent)
            Timber.i("Metrics exported to CSV: %s", file.absolutePath)
            ExportedMetrics.Success(
                filePath = file.absolutePath,
                fileName = fileName,
                format = "csv",
            )
        } catch (e: Exception) {
            Timber.e(e, "CSV export failed")
            ExportedMetrics.Error("CSV export failed: ${e.message}")
        }

    override suspend fun exportToJSON(metrics: List<Map<String, Any>>): ExportedMetrics =
        try {
            val timestamp = timeFormat.format(Date())
            val fileName = "metrics_$timestamp.json"
            val file = File(context.cacheDir, fileName)
            val jsonContent = buildJSONContent(metrics)
            file.writeText(jsonContent)
            Timber.i("Metrics exported to JSON: %s", file.absolutePath)
            ExportedMetrics.Success(
                filePath = file.absolutePath,
                fileName = fileName,
                format = "json",
            )
        } catch (e: Exception) {
            Timber.e(e, "JSON export failed")
            ExportedMetrics.Error("JSON export failed: ${e.message}")
        }

    private fun buildCSVContent(metrics: List<Map<String, Any>>): String {
        if (metrics.isEmpty()) return ""
        val keys = metrics.first().keys.toList()
        val header = keys.joinToString(",") + "\n"
        val rows =
            metrics.joinToString("\n") { metric ->
                keys.joinToString(",") { key -> metric[key]?.toString() ?: "" }
            }
        return header + rows
    }

    private fun buildJSONContent(metrics: List<Map<String, Any>>): String {
        val metricsJson =
            metrics.joinToString(",\n") { metric ->
                metric.entries.joinToString(
                    ",",
                    "{\n",
                    "\n}",
                ) { (key, value) ->
                    "    \"$key\": ${formatJsonValue(value)}"
                }
            }
        return "[\n$metricsJson\n]"
    }

    private fun formatJsonValue(value: Any?): String =
        when (value) {
            is String -> "\"$value\""
            is Number -> value.toString()
            else -> "\"${value?.toString()}\""
        }
}

/**
 * Factory function for creating MetricsExporter instances.
 *
 * Used by Hilt to provide MetricsExporter dependency.
 */
fun MetricsExporter(context: Context): MetricsExporter = MetricsExporterImpl(context)
