package com.bs.threadsimulator.domain

import com.bs.threadsimulator.common.MetricsExporter
import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.model.ExportedMetrics
import javax.inject.Inject

/**
 * Use case for exporting collected thread metrics.
 *
 * Coordinates metrics export operations, accepting format parameter
 * and returning success/error results.
 */
class ExportMetricsUseCase
    @Inject
    constructor(
        private val threadMonitor: ThreadMonitor,
        private val metricsExporter: MetricsExporter,
    ) {
        /**
         * Exports metrics in the specified format.
         *
         * @param format Export format: "csv" or "json"
         * @return ExportedMetrics.Success with file details or ExportedMetrics.Error
         */
        suspend fun execute(format: String): ExportedMetrics {
            val metrics = threadMonitor.getMetricsSnapshot()

            return when (format.lowercase()) {
                "csv" -> metricsExporter.exportToCSV(metrics)
                "json" -> metricsExporter.exportToJSON(metrics)
                else -> ExportedMetrics.Error("Unsupported format: $format")
            }
        }
    }
