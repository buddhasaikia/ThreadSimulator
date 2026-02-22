package com.bs.threadsimulator.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

/**
 * Data class representing thread execution metrics.
 *
 * Tracks performance metrics for a specific thread and update type (PE, CurrentPrice, HighLow).
 *
 * @property threadId The unique identifier of the thread
 * @property threadName The human-readable name of the thread
 * @property updateType The type of update being tracked (e.g., "PE", "CurrentPrice", "HighLow")
 * @property updateCount Total number of updates performed by this thread
 * @property avgUpdateTimeMs Average time in milliseconds per update operation
 */
data class ThreadMetrics(
    val threadId: Long,
    val threadName: String,
    val updateType: String,
    val updateCount: Long,
    val avgUpdateTimeMs: Long
)

/**
 * Monitor for tracking thread execution metrics across concurrent data updates.
 *
 * [ThreadMonitor] records update counts and timings for each thread and update type,
 * providing real-time visibility into multi-threaded performance. Useful for debugging
 * threading issues and monitoring performance bottlenecks in concurrent operations.
 */
class ThreadMonitor @Inject constructor() {
    private val _metrics = MutableStateFlow<List<ThreadMetrics>>(emptyList())

    /**
     * StateFlow of current thread metrics.
     *
     * Emits whenever metrics are updated. Consumers can collect this flow to observe
     * real-time changes in thread execution metrics.
     */
    val metrics: StateFlow<List<ThreadMetrics>> = _metrics.asStateFlow()
    
    private val updateCounts = ConcurrentHashMap<String, AtomicLong>()
    private val updateTimes = ConcurrentHashMap<String, AtomicLong>()
    private val threadNames = ConcurrentHashMap<Long, String>()
    
    /**
     * Records an update operation with its execution time.
     *
     * Thread-safe. Should be called by worker threads to log their update operations.
     * Synchronized to prevent race conditions with [clearMetrics] operations.
     *
     * @param updateType The type of update (e.g., "PE", "CurrentPrice", "HighLow")
     * @param updateTimeMs The time taken for this update operation in milliseconds
     */
    @Synchronized
    fun recordUpdate(updateType: String, updateTimeMs: Long) {
        val thread = Thread.currentThread()
        val key = "${thread.id}_${updateType}"
        
        threadNames.putIfAbsent(thread.id, thread.name)
        updateCounts.getOrPut(key) { AtomicLong(0) }.incrementAndGet()
        updateTimes.getOrPut(key) { AtomicLong(0) }.addAndGet(updateTimeMs)
        
        updateMetrics()
    }
    
    private fun updateMetrics() {
        val currentMetrics = updateCounts.keys.map { key ->
            val (threadId, updateType) = key.split("_", limit = 2)
            val threadIdLong = threadId.toLong()
            val count = updateCounts[key]?.get() ?: 0
            val totalTime = updateTimes[key]?.get() ?: 0
            
            ThreadMetrics(
                threadId = threadIdLong,
                threadName = threadNames[threadIdLong] ?: "Unknown",
                updateType = updateType,
                updateCount = count,
                avgUpdateTimeMs = if (count > 0) totalTime / count else 0
            )
        }
        _metrics.value = currentMetrics
    }
    
    /**
     * Clears all accumulated metrics.
     *
     * Useful for resetting metrics when starting a new simulation or test.
     * Thread-safe. Synchronized to ensure all three maps are cleared atomically,
     * preventing inconsistent state if [recordUpdate] is called concurrently.
     */
    @Synchronized
    fun clearMetrics() {
        updateCounts.clear()
        updateTimes.clear()
        threadNames.clear()
        _metrics.value = emptyList()
    }
}