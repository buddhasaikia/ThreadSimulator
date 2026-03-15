package com.bs.threadsimulator.domain

import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.model.ExportedMetrics
import com.bs.threadsimulator.model.MetricsExporter
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
            val metricsData =
                metrics.map { metric ->
                    mapOf(
                        "threadId" to metric.threadId,
                        "threadName" to metric.threadName,
                        "updateType" to metric.updateType,
                        "updateCount" to metric.updateCount,
                        "avgUpdateTimeMs" to metric.avgUpdateTimeMs,
                        "peakUpdatesPerSec" to metric.peakUpdatesPerSec,
                        "stateTransitions" to metric.stateTransitions,
                        "queueDepth" to metric.queueDepth,
                        "threadAllocatedBytes" to metric.threadAllocatedBytes,
                        "jitterMs" to metric.jitterMs,
                    )
                }

            return when (format.lowercase()) {
                "csv" -> metricsExporter.exportToCSV(metricsData)
                "json" -> metricsExporter.exportToJSON(metricsData)
                else -> ExportedMetrics.Error("Unsupported format: $format")
            }
        }
    }
