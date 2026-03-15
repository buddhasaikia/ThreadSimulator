package com.bs.threadsimulator.model

/**
 * Interface for exporting thread metrics to various formats.
 *
 * Implementations handle serialization and file writing for metrics snapshots.
 */
interface MetricsExporter {
    /**
     * Exports metrics to CSV format.
     *
     * @param metrics List of thread metrics data (serialized as strings)
     * @return ExportedMetrics.Success with file path or ExportedMetrics.Error
     */
    suspend fun exportToCSV(metrics: List<Map<String, Any>>): ExportedMetrics

    /**
     * Exports metrics to JSON format.
     *
     * @param metrics List of thread metrics data (serialized as strings)
     * @return ExportedMetrics.Success with file path or ExportedMetrics.Error
     */
    suspend fun exportToJSON(metrics: List<Map<String, Any>>): ExportedMetrics
}
