package com.bs.threadsimulator.feature.stockdata.domain

import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.model.ExportedMetrics
import com.bs.threadsimulator.model.MetricsExporter
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for exporting thread metrics to CSV and JSON formats.
 *
 * [ExportMetricsUseCase] orchestrates the snapshot of metrics from [ThreadMonitor]
 * and delegates to [MetricsExporter] for file writing. It handles errors gracefully
 * and logs all operations.
 *
 * Usage:
 * ```
 * val csvResult = exportMetricsUseCase.executeCSV()
 * val jsonResult = exportMetricsUseCase.executeJSON()
 * ```
 */
class ExportMetricsUseCase
    @Inject
    constructor(
        private val metricsExporter: MetricsExporter,
        private val threadMonitor: ThreadMonitor,
    ) {
        /**
         * Exports current thread metrics to CSV format.
         *
         * Takes a snapshot of ThreadMonitor metrics, converts them to exportable format,
         * and calls MetricsExporter.exportToCSV(). Logs all operations and handles errors.
         *
         * @return ExportedMetrics.Success with file details or ExportedMetrics.Error with message
         */
        suspend fun executeCSV(): ExportedMetrics =
            try {
                Timber.d("Starting CSV export of thread metrics")
                val metricsSnapshot = threadMonitor.getMetricsSnapshot()
                Timber.i("Metrics snapshot captured: %d entries", metricsSnapshot.size)

                if (metricsSnapshot.isEmpty()) {
                    Timber.w("No metrics available for export")
                    ExportedMetrics.Error("No metrics available for export")
                } else {
                    val metricsData =
                        metricsSnapshot.map { metrics ->
                            mapOf(
                                "threadId" to metrics.threadId,
                                "threadName" to metrics.threadName,
                                "updateType" to metrics.updateType,
                                "updateCount" to metrics.updateCount,
                                "avgUpdateTimeMs" to metrics.avgUpdateTimeMs,
                                "peakUpdatesPerSec" to metrics.peakUpdatesPerSec,
                                "stateTransitions" to metrics.stateTransitions,
                                "queueDepth" to metrics.queueDepth,
                                "threadAllocatedBytes" to metrics.threadAllocatedBytes,
                                "jitterMs" to metrics.jitterMs,
                            )
                        }

                    val result = metricsExporter.exportToCSV(metricsData)
                    when (result) {
                        is ExportedMetrics.Success -> {
                            Timber.i("CSV export succeeded: %s", result.filePath)
                        }
                        is ExportedMetrics.Error -> {
                            Timber.e("CSV export failed: %s", result.message)
                        }
                    }
                    result
                }
            } catch (e: Exception) {
                Timber.e(e, "CSV export failed with exception")
                ExportedMetrics.Error("CSV export failed: ${e.message}")
            }

        /**
         * Exports current thread metrics to JSON format.
         *
         * Takes a snapshot of ThreadMonitor metrics, converts them to exportable format,
         * and calls MetricsExporter.exportToJSON(). Logs all operations and handles errors.
         *
         * @return ExportedMetrics.Success with file details or ExportedMetrics.Error with message
         */
        suspend fun executeJSON(): ExportedMetrics =
            try {
                Timber.d("Starting JSON export of thread metrics")
                val metricsSnapshot = threadMonitor.getMetricsSnapshot()
                Timber.i("Metrics snapshot captured: %d entries", metricsSnapshot.size)

                if (metricsSnapshot.isEmpty()) {
                    Timber.w("No metrics available for export")
                    ExportedMetrics.Error("No metrics available for export")
                } else {
                    val metricsData =
                        metricsSnapshot.map { metrics ->
                            mapOf(
                                "threadId" to metrics.threadId,
                                "threadName" to metrics.threadName,
                                "updateType" to metrics.updateType,
                                "updateCount" to metrics.updateCount,
                                "avgUpdateTimeMs" to metrics.avgUpdateTimeMs,
                                "peakUpdatesPerSec" to metrics.peakUpdatesPerSec,
                                "stateTransitions" to metrics.stateTransitions,
                                "queueDepth" to metrics.queueDepth,
                                "threadAllocatedBytes" to metrics.threadAllocatedBytes,
                                "jitterMs" to metrics.jitterMs,
                            )
                        }

                    val result = metricsExporter.exportToJSON(metricsData)
                    when (result) {
                        is ExportedMetrics.Success -> {
                            Timber.i("JSON export succeeded: %s", result.filePath)
                        }
                        is ExportedMetrics.Error -> {
                            Timber.e("JSON export failed: %s", result.message)
                        }
                    }
                    result
                }
            } catch (e: Exception) {
                Timber.e(e, "JSON export failed with exception")
                ExportedMetrics.Error("JSON export failed: ${e.message}")
            }
    }
