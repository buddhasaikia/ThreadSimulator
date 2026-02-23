package com.bs.threadsimulator.model

/**
 * Result of a metrics export operation.
 *
 * Represents the outcome of exporting metrics to a file.
 */
sealed class ExportedMetrics {
    /**
     * Successful export operation.
     *
     * @property fileName The name of the exported file
     * @property filePath The full path to the exported file
     * @property format The export format used (csv, json)
     */
    data class Success(
        val fileName: String,
        val filePath: String,
        val format: String,
    ) : ExportedMetrics()

    /**
     * Failed export operation.
     *
     * @property message Error message describing the failure
     */
    data class Error(val message: String) : ExportedMetrics()
}
