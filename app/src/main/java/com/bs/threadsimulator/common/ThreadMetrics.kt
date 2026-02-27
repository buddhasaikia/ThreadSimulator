package com.bs.threadsimulator.common

import android.os.Debug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlin.math.sqrt

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
 * @property peakUpdatesPerSec Highest updates-per-second rate observed for this thread+type
 * @property stateTransitions Count of thread state changes (e.g., RUNNABLE↔WAITING)
 * @property queueDepth Current channel buffer occupancy (shared across threads)
 * @property threadAllocatedBytes Cumulative bytes allocated by this thread (-1 if unavailable)
 * @property jitterMs Standard deviation of update interval times in milliseconds
 */
data class ThreadMetrics(
    val threadId: Long,
    val threadName: String,
    val updateType: String,
    val updateCount: Long,
    val avgUpdateTimeMs: Long,
    val peakUpdatesPerSec: Double = 0.0,
    val stateTransitions: Int = 0,
    val queueDepth: Int = 0,
    val threadAllocatedBytes: Long = -1L,
    val jitterMs: Double = 0.0,
)

/**
 * Monitor for tracking thread execution metrics across concurrent data updates.
 *
 * [ThreadMonitor] records update counts and timings for each thread and update type,
 * providing real-time visibility into multi-threaded performance. Useful for debugging
 * threading issues and monitoring performance bottlenecks in concurrent operations.
 *
 * Advanced metrics include peak updates/sec, thread state transitions, queue depth,
 * per-thread memory allocation, and jitter (std-dev of update intervals).
 */
class ThreadMonitor
    @Inject
    constructor() {
        private val _metrics = MutableStateFlow<List<ThreadMetrics>>(emptyList())

        /**
         * StateFlow of current thread metrics.
         *
         * Emits whenever metrics are updated. Consumers can collect this flow to observe
         * real-time changes in thread execution metrics.
         */
        val metrics: StateFlow<List<ThreadMetrics>> = _metrics.asStateFlow()

        // --- Basic tracking ---
        private val updateCounts = ConcurrentHashMap<String, AtomicLong>()
        private val updateTimes = ConcurrentHashMap<String, AtomicLong>()
        private val threadNames = ConcurrentHashMap<Long, String>()

        // --- Advanced tracking ---

        /** Timestamps of each update for peak-UPS and jitter computation. */
        private val updateTimestamps = ConcurrentHashMap<String, MutableList<Long>>()

        /** Per-thread state transition counter. */
        private val stateTransitionCounts = ConcurrentHashMap<Long, AtomicInteger>()

        /** Previous thread state for detecting transitions. */
        private val lastThreadStates = ConcurrentHashMap<Long, Thread.State>()

        /** Shared channel queue depth counter. */
        private val queueDepthCounter = AtomicInteger(0)

        /**
         * Increments the queue depth counter.
         * Should be called after successfully sending an element to the channel.
         */
        fun incrementQueueDepth() {
            queueDepthCounter.incrementAndGet()
        }

        /**
         * Decrements the queue depth counter.
         * Should be called when an element is received from the channel.
         */
        fun decrementQueueDepth() {
            queueDepthCounter.decrementAndGet()
        }

        /**
         * Records an update operation with its execution time.
         *
         * Thread-safe. Should be called by worker threads to log their update operations.
         * Synchronized to prevent race conditions with [clearMetrics] operations.
         *
         * Also tracks thread state transitions, timestamps for peak-UPS/jitter, and
         * per-thread memory allocation.
         *
         * @param updateType The type of update (e.g., "PE", "CurrentPrice", "HighLow")
         * @param updateTimeMs The time taken for this update operation in milliseconds
         */
        @Synchronized
        fun recordUpdate(
            updateType: String,
            updateTimeMs: Long,
        ) {
            val thread = Thread.currentThread()
            val key = "${thread.id}_$updateType"

            threadNames.putIfAbsent(thread.id, thread.name)
            updateCounts.getOrPut(key) { AtomicLong(0) }.incrementAndGet()
            updateTimes.getOrPut(key) { AtomicLong(0) }.addAndGet(updateTimeMs)

            // Record timestamp for peak-UPS and jitter with bounded 10-second retention window
            val now = System.currentTimeMillis()
            val timestamps = updateTimestamps.getOrPut(key) { mutableListOf() }
            timestamps.add(now)

            val retentionWindowMs = 10_000L
            val cutoff = now - retentionWindowMs
            while (timestamps.isNotEmpty() && timestamps[0] < cutoff) {
                timestamps.removeAt(0)
            }

            // Track state transitions
            val currentState = thread.state
            val previousState = lastThreadStates.put(thread.id, currentState)
            if (previousState != null && previousState != currentState) {
                stateTransitionCounts.getOrPut(thread.id) { AtomicInteger(0) }.incrementAndGet()
            }

            updateMetrics()
        }

        private fun updateMetrics() {
            val currentQueueDepth = queueDepthCounter.get()
            val currentMetrics =
                updateCounts.keys.map { key ->
                    val (threadId, updateType) = key.split("_", limit = 2)
                    val threadIdLong = threadId.toLong()
                    val count = updateCounts[key]?.get() ?: 0
                    val totalTime = updateTimes[key]?.get() ?: 0
                    val timestamps = updateTimestamps[key] ?: emptyList()

                    ThreadMetrics(
                        threadId = threadIdLong,
                        threadName = threadNames[threadIdLong] ?: "Unknown",
                        updateType = updateType,
                        updateCount = count,
                        avgUpdateTimeMs = if (count > 0) totalTime / count else 0,
                        peakUpdatesPerSec = computePeakUpdatesPerSec(timestamps),
                        stateTransitions = stateTransitionCounts[threadIdLong]?.get() ?: 0,
                        queueDepth = currentQueueDepth,
                        threadAllocatedBytes = getThreadAllocatedBytes(threadIdLong),
                        jitterMs = computeJitter(timestamps),
                    )
                }
            _metrics.value = currentMetrics
        }

        /**
         * Computes peak updates per second.
         * Runs in O(n) time using a sliding window over the monotonically increasing timestamps.
         */
        private fun computePeakUpdatesPerSec(timestamps: List<Long>): Double {
            if (timestamps.isEmpty()) return 0.0
            if (timestamps.size == 1) return 1.0

            val windowMillis = 1000L
            var peak = 0
            var start = 0

            for (end in timestamps.indices) {
                val windowStart = timestamps[end] - windowMillis

                while (start < end && timestamps[start] <= windowStart) {
                    start++
                }

                val count = end - start + 1
                if (count > peak) {
                    peak = count
                }
            }
            return peak.toDouble()
        }

        /**
         * Computes jitter (standard deviation) of inter-update intervals in milliseconds.
         */
        private fun computeJitter(timestamps: List<Long>): Double {
            if (timestamps.size < 2) return 0.0
            val intervals =
                (1 until timestamps.size).map { i ->
                    (timestamps[i] - timestamps[i - 1]).toDouble()
                }
            val mean = intervals.average()
            val variance = intervals.map { (it - mean) * (it - mean) }.average()
            return sqrt(variance)
        }

        /**
         * Returns the process-wide native heap allocated size in bytes.
         * Android does not support per-thread memory tracking, so this is
         * a process-level metric shared across all threads.
         * Returns -1 if unavailable.
         */
        private fun getThreadAllocatedBytes(
            @Suppress("UNUSED_PARAMETER") threadId: Long,
        ): Long =
            try {
                Debug.getNativeHeapAllocatedSize()
            } catch (_: Exception) {
                -1L
            }

        /**
         * Clears all accumulated metrics.
         *
         * Useful for resetting metrics when starting a new simulation or test.
         * Thread-safe. Synchronized to ensure all maps are cleared atomically,
         * preventing inconsistent state if [recordUpdate] is called concurrently.
         */
        @Synchronized
        fun clearMetrics() {
            updateCounts.clear()
            updateTimes.clear()
            threadNames.clear()
            updateTimestamps.clear()
            stateTransitionCounts.clear()
            lastThreadStates.clear()
            queueDepthCounter.set(0)
            _metrics.value = emptyList()
        }

        /**
         * Gets a snapshot of current metrics for export purposes.
         *
         * Thread-safe. Returns a copy of metrics to avoid concurrent modification issues.
         *
         * @return A list of current ThreadMetrics
         */
        @Synchronized
        fun getMetricsSnapshot(): List<ThreadMetrics> = _metrics.value.toList()
    }
