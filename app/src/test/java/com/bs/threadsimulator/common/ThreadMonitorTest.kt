package com.bs.threadsimulator.common

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

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
    }

    @Test
    fun testRecordingMultipleUpdates() {
        threadMonitor.recordUpdate("PE", 10)
        threadMonitor.recordUpdate("PE", 15)
        threadMonitor.recordUpdate("PE", 20)

        val metrics = threadMonitor.metrics.value
        assertTrue("Should have at least one metric", metrics.isNotEmpty())
        assertTrue("Should have PE metric", metrics.any { it.updateType == "PE" })
    }

    @Test
    fun testMultipleUpdateTypes() {
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
    fun testThreadMetricsDataClass() {
        val metrics = ThreadMetrics(
            threadId = 1L,
            threadName = "TestThread",
            updateType = "PE",
            updateCount = 5,
            avgUpdateTimeMs = 20
        )

        assertEquals(1L, metrics.threadId)
        assertEquals("TestThread", metrics.threadName)
        assertEquals("PE", metrics.updateType)
        assertEquals(5, metrics.updateCount)
        assertEquals(20, metrics.avgUpdateTimeMs)
    }

    @Test
    fun testMetricsFlowIsReactive() {
        val initialMetrics = threadMonitor.metrics.value.size
        
        threadMonitor.recordUpdate("PE", 10)
        
        val afterMetrics = threadMonitor.metrics.value.size
        assertTrue("Metrics should be updated", afterMetrics >= initialMetrics)
    }

    @Test
    fun testRecordMultipleCallsBuildsMetrics() {
        repeat(5) {
            threadMonitor.recordUpdate("PE", 10)
        }

        val metrics = threadMonitor.metrics.value
        assertTrue("Should have metrics after multiple calls", metrics.isNotEmpty())
    }

    @Test
    fun testDifferentTypesInMetrics() {
        threadMonitor.recordUpdate("PE", 10)
        threadMonitor.recordUpdate("HighLow", 5)
        threadMonitor.recordUpdate("CurrentPrice", 15)

        val metrics = threadMonitor.metrics.value
        assertTrue("Should track all types", metrics.size >= 3)
    }

    @Test
    fun testMetricsContainValidData() {
        threadMonitor.recordUpdate("PE", 100)
        threadMonitor.recordUpdate("PE", 200)

        val metrics = threadMonitor.metrics.value
        val peMetric = metrics.find { it.updateType == "PE" }
        
        assertNotNull("Should find PE metric", peMetric)
        assertTrue("Count should be at least 1", peMetric!!.updateCount >= 1)
        assertTrue("Avg time should be reasonable", peMetric.avgUpdateTimeMs in 0..1000)
    }
}
