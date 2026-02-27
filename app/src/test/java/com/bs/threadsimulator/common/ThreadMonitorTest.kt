package com.bs.threadsimulator.common

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ThreadMonitorTest {
    private lateinit var threadMonitor: ThreadMonitor

    @Before
    fun setup() {
        threadMonitor = ThreadMonitor()
    }

    @Test
    fun testRecordUpdateEmitsMetrics() {
        threadMonitor.recordUpdate("PE", 10)

        val metrics = threadMonitor.metrics.value
        assertTrue("Should have recorded metrics", metrics.isNotEmpty())
        assertTrue("Should have PE metric", metrics.any { it.updateType == "PE" })
    }

    @Test
    fun testRecordingMultipleUpdatesTracksData() {
        threadMonitor.recordUpdate("PE", 10)
        threadMonitor.recordUpdate("PE", 15)

        val metrics = threadMonitor.metrics.value
        assertTrue("Should have at least one metric", metrics.isNotEmpty())
        val peMetric = metrics.find { it.updateType == "PE" }
        assertNotNull("PE metric should exist", peMetric)
        assertTrue("Count should be tracked", peMetric!!.updateCount > 0)
    }

    @Test
    fun testMultipleUpdateTypesTracked() {
        threadMonitor.recordUpdate("PE", 10)
        threadMonitor.recordUpdate("CurrentPrice", 5)

        val metrics = threadMonitor.metrics.value
        assertTrue("Should track different types", metrics.any { it.updateType == "PE" })
        assertTrue("Should track CurrentPrice", metrics.any { it.updateType == "CurrentPrice" })
    }

    @Test
    fun testMetricsAreNotNull() {
        threadMonitor.recordUpdate("HighLow", 25)

        val metrics = threadMonitor.metrics.value
        metrics.forEach { metric ->
            assertNotNull("Metric should have threadId", metric.threadId)
            assertNotNull("Metric should have updateType", metric.updateType)
            assertTrue("Metric should have positive or zero count", metric.updateCount >= 0)
            assertTrue("Metric should have non-negative avg time", metric.avgUpdateTimeMs >= 0)
        }
    }

    @Test
    fun testAverageTimeIsCalculated() {
        threadMonitor.recordUpdate("PE", 100)
        threadMonitor.recordUpdate("PE", 200)

        val metrics = threadMonitor.metrics.value
        val peMetric = metrics.find { it.updateType == "PE" }
        assertNotNull("PE metric should exist", peMetric)
        assertTrue("Average time should be calculated", peMetric!!.avgUpdateTimeMs > 0)
    }

    @Test
    fun testThreadMetricsDataClass() {
        val metrics =
            ThreadMetrics(
                threadId = 1L,
                threadName = "TestThread",
                updateType = "PE",
                updateCount = 5,
                avgUpdateTimeMs = 20,
                peakUpdatesPerSec = 10.0,
                stateTransitions = 3,
                queueDepth = 7,
                threadAllocatedBytes = 1024L,
                jitterMs = 2.5,
            )

        assertEquals(1L, metrics.threadId)
        assertEquals("TestThread", metrics.threadName)
        assertEquals("PE", metrics.updateType)
        assertEquals(5, metrics.updateCount)
        assertEquals(20, metrics.avgUpdateTimeMs)
        assertEquals(10.0, metrics.peakUpdatesPerSec, 0.01)
        assertEquals(3, metrics.stateTransitions)
        assertEquals(7, metrics.queueDepth)
        assertEquals(1024L, metrics.threadAllocatedBytes)
        assertEquals(2.5, metrics.jitterMs, 0.01)
    }

    @Test
    fun testMetricsFlowIsReactive() {
        val initialSize = threadMonitor.metrics.value.size

        threadMonitor.recordUpdate("PE", 10)

        val afterSize = threadMonitor.metrics.value.size
        assertTrue("Metrics should be updated", afterSize >= initialSize)
    }

    @Test
    fun testRecordMultipleCallsBuildsMetrics() {
        repeat(5) {
            threadMonitor.recordUpdate("PE", 10)
        }

        val metrics = threadMonitor.metrics.value
        assertTrue("Should have metrics after multiple calls", metrics.isNotEmpty())
        val peMetric = metrics.find { it.updateType == "PE" }
        assertNotNull("PE metric should exist", peMetric)
        assertEquals("Count should be 5", 5L, peMetric!!.updateCount)
    }

    @Test
    fun testDifferentTypesAreTrackedIndependently() {
        threadMonitor.recordUpdate("PE", 100)
        threadMonitor.recordUpdate("CurrentPrice", 50)

        val metrics = threadMonitor.metrics.value
        val peMetric = metrics.find { it.updateType == "PE" }
        val priceMetric = metrics.find { it.updateType == "CurrentPrice" }

        assertTrue("PE should be tracked", peMetric != null)
        assertTrue("CurrentPrice should be tracked", priceMetric != null)
    }

    @Test
    fun testMetricsContainValidData() {
        threadMonitor.recordUpdate("PE", 100)
        threadMonitor.recordUpdate("PE", 200)

        val metrics = threadMonitor.metrics.value
        val peMetric = metrics.find { it.updateType == "PE" }

        assertNotNull("Should find PE metric", peMetric)
        assertEquals("Count should be 2", 2L, peMetric!!.updateCount)
        assertEquals("Avg time should be 150", 150L, peMetric.avgUpdateTimeMs)
    }

    @Test
    fun testMetricsFlowEmitsOnRecordUpdate() =
        runTest {
            threadMonitor.metrics.test {
                val initial = awaitItem()
                assertTrue("Initial metrics should be empty", initial.isEmpty())

                threadMonitor.recordUpdate("PE", 100)
                val afterUpdate = awaitItem()
                assertTrue("Should emit updated metrics", afterUpdate.isNotEmpty())
                assertEquals("PE", afterUpdate.first().updateType)
                assertEquals(1L, afterUpdate.first().updateCount)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- Advanced metrics tests ---

    @Test
    fun testPeakUpdatesPerSecIsTracked() {
        // Record several updates rapidly — they all happen in the same millisecond range
        repeat(10) {
            threadMonitor.recordUpdate("PE", 5)
        }

        val metrics = threadMonitor.metrics.value
        val peMetric = metrics.find { it.updateType == "PE" }
        assertNotNull("PE metric should exist", peMetric)
        assertTrue(
            "Peak UPS should be >= 10 (all within 1s window)",
            peMetric!!.peakUpdatesPerSec >= 10.0,
        )
    }

    @Test
    fun testQueueDepthIncrementAndDecrement() {
        threadMonitor.incrementQueueDepth()
        threadMonitor.incrementQueueDepth()
        threadMonitor.incrementQueueDepth()

        // Record an update to trigger metrics refresh
        threadMonitor.recordUpdate("PE", 10)
        var metrics = threadMonitor.metrics.value
        var peMetric = metrics.find { it.updateType == "PE" }
        assertEquals("Queue depth should be 3", 3, peMetric!!.queueDepth)

        threadMonitor.decrementQueueDepth()
        threadMonitor.recordUpdate("PE", 10)
        metrics = threadMonitor.metrics.value
        peMetric = metrics.find { it.updateType == "PE" }
        assertEquals("Queue depth should be 2 after decrement", 2, peMetric!!.queueDepth)
    }

    @Test
    fun testJitterIsComputedForMultipleUpdates() {
        // Rapid calls — jitter should be small or zero
        repeat(5) {
            threadMonitor.recordUpdate("PE", 10)
        }

        val metrics = threadMonitor.metrics.value
        val peMetric = metrics.find { it.updateType == "PE" }
        assertNotNull("PE metric should exist", peMetric)
        assertTrue(
            "Jitter should be non-negative",
            peMetric!!.jitterMs >= 0.0,
        )
    }

    @Test
    fun testThreadAllocatedBytesIsPopulated() {
        threadMonitor.recordUpdate("PE", 10)

        val metrics = threadMonitor.metrics.value
        val peMetric = metrics.find { it.updateType == "PE" }
        assertNotNull("PE metric should exist", peMetric)
        // threadAllocatedBytes is either >= 0 (supported) or -1 (unsupported)
        assertTrue(
            "Memory should be -1 or a positive value",
            peMetric!!.threadAllocatedBytes == -1L || peMetric.threadAllocatedBytes > 0,
        )
    }

    @Test
    fun testClearMetricsResetsAdvancedFields() {
        threadMonitor.incrementQueueDepth()
        threadMonitor.incrementQueueDepth()
        threadMonitor.recordUpdate("PE", 100)

        // Verify metrics exist
        assertTrue(threadMonitor.metrics.value.isNotEmpty())

        threadMonitor.clearMetrics()

        assertTrue("Metrics should be empty after clear", threadMonitor.metrics.value.isEmpty())

        // Record again and verify fresh state
        threadMonitor.recordUpdate("PE", 50)
        val metrics = threadMonitor.metrics.value
        val peMetric = metrics.find { it.updateType == "PE" }
        assertNotNull(peMetric)
        assertEquals("Count should be 1 after clear+record", 1L, peMetric!!.updateCount)
        assertEquals("Queue depth should be 0 after clear", 0, peMetric.queueDepth)
    }

    @Test
    fun testThreadMetricsDefaultValues() {
        val metrics =
            ThreadMetrics(
                threadId = 1L,
                threadName = "Test",
                updateType = "PE",
                updateCount = 1,
                avgUpdateTimeMs = 10,
            )

        assertEquals("Default peakUpdatesPerSec should be 0.0", 0.0, metrics.peakUpdatesPerSec, 0.01)
        assertEquals("Default stateTransitions should be 0", 0, metrics.stateTransitions)
        assertEquals("Default queueDepth should be 0", 0, metrics.queueDepth)
        assertEquals("Default threadAllocatedBytes should be -1", -1L, metrics.threadAllocatedBytes)
        assertEquals("Default jitterMs should be 0.0", 0.0, metrics.jitterMs, 0.01)
    }
}
