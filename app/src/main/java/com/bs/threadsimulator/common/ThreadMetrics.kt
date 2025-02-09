package com.bs.threadsimulator.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

data class ThreadMetrics(
    val threadId: Long,
    val threadName: String,
    val updateType: String,
    val updateCount: Long,
    val avgUpdateTimeMs: Long
)

class ThreadMonitor @Inject constructor() {
    private val _metrics = MutableStateFlow<List<ThreadMetrics>>(emptyList())
    val metrics: StateFlow<List<ThreadMetrics>> = _metrics.asStateFlow()
    
    private val updateCounts = ConcurrentHashMap<String, AtomicLong>()
    private val updateTimes = ConcurrentHashMap<String, AtomicLong>()
    
    fun recordUpdate(updateType: String, updateTimeMs: Long) {
        val thread = Thread.currentThread()
        val key = "${thread.id}_${updateType}"
        
        updateCounts.getOrPut(key) { AtomicLong(0) }.incrementAndGet()
        updateTimes.getOrPut(key) { AtomicLong(0) }.addAndGet(updateTimeMs)
        
        updateMetrics()
    }
    
    private fun updateMetrics() {
        val currentMetrics = updateCounts.keys.map { key ->
            val (threadId, updateType) = key.split("_")
            val count = updateCounts[key]?.get() ?: 0
            val totalTime = updateTimes[key]?.get() ?: 0
            
            ThreadMetrics(
                threadId = threadId.toLong(),
                threadName = Thread.currentThread().name,
                updateType = updateType,
                updateCount = count,
                avgUpdateTimeMs = if (count > 0) totalTime / count else 0
            )
        }
        _metrics.value = currentMetrics
    }
}