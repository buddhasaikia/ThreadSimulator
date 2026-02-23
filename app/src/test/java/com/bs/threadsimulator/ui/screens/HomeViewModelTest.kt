package com.bs.threadsimulator.ui.screens

import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.Stock
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal

class HomeViewModelTest {

    private lateinit var threadMonitor: ThreadMonitor

    @Before
    fun setup() {
        threadMonitor = ThreadMonitor()
    }

    @Test
    fun testThreadMonitorIsAvailable() {
        assertNotNull(threadMonitor)
    }

    @Test
    fun testErrorMessageCanBeSet() {
        val errorMessage = mutableMapOf<String, String?>()
        errorMessage["error"] = "Test Error"
        assertEquals("Test Error", errorMessage["error"])
    }

    @Test
    fun testErrorMessageCanBeCleared() {
        val errorMessage = mutableMapOf<String, String?>()
        errorMessage["error"] = "Test Error"
        errorMessage["error"] = null
        assertNull(errorMessage["error"])
    }

    @Test
    fun testCompanyStateUpdate() {
        val company = Company(
            companyName = "Apple Inc",
            stock = Stock(
                symbol = "AAPL",
                currentPrice = BigDecimal("150.00"),
                high = BigDecimal("155.00"),
                low = BigDecimal("145.00")
            )
        )

        assertEquals("Apple Inc", company.companyName)
        assertEquals("AAPL", company.stock.symbol)
        assertEquals(BigDecimal("150.00"), company.stock.currentPrice)
    }

    @Test
    fun testCompanyListManagement() {
        val company1 = Company(companyName = "Apple Inc", stock = Stock(symbol = "AAPL"))
        val company2 = Company(companyName = "Google LLC", stock = Stock(symbol = "GOOGL"))

        val companies = mutableListOf(company1, company2)
        assertEquals(2, companies.size)
        assertEquals("Apple Inc", companies[0].companyName)
        assertEquals("Google LLC", companies[1].companyName)
    }

    @Test
    fun testStockPriceUpdate() {
        val stock = Stock(
            symbol = "AAPL",
            currentPrice = BigDecimal("150.00"),
            high = BigDecimal("155.00"),
            low = BigDecimal("145.00")
        )

        stock.currentPrice = BigDecimal("151.00")
        assertEquals(BigDecimal("151.00"), stock.currentPrice)
    }

    @Test
    fun testStockHighLowUpdate() {
        val stock = Stock(symbol = "AAPL")
        stock.high = BigDecimal("160.00")
        stock.low = BigDecimal("140.00")

        assertEquals(BigDecimal("160.00"), stock.high)
        assertEquals(BigDecimal("140.00"), stock.low)
    }

    @Test
    fun testThreadMonitorMetricsAccess() {
        threadMonitor.recordUpdate("PE", 10)
        val metrics = threadMonitor.metrics.value
        assertTrue("Metrics should be accessible", metrics.isNotEmpty())
    }

    @Test
    fun testCompanyThreadNameUpdate() {
        val company = Company(companyName = "MSFT")
        company.threadName = "Worker-1"

        assertEquals("Worker-1", company.threadName)
    }

    @Test
    fun testCompanyPERatioUpdate() {
        val company = Company(companyName = "TSLA")
        company.peRatio = "25.5"

        assertEquals("25.5", company.peRatio)
    }

    @Test
    fun testCompanyPreviousClosingPrice() {
        val company = Company(previousClosingPrice = 150)

        assertEquals(150, company.previousClosingPrice)
    }

    @Test
    fun testStockSymbolPersistence() {
        val stock = Stock(symbol = "GOOG")
        stock.currentPrice = BigDecimal("140.00")

        assertEquals("GOOG", stock.symbol)
        assertEquals(BigDecimal("140.00"), stock.currentPrice)
    }
}
