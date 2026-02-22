package com.bs.threadsimulator.ui.screens

import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.Stock
import java.math.BigDecimal
import org.junit.Test
import org.junit.Assert.*

class HomeViewModelTest {

    @Test
    fun testCompanyStateUpdate() {
        val company = Company(
            companyName = "Apple Inc",
            stock = Stock(
                symbol = "AAPL",
                currentPrice = BigDecimal(150.0),
                high = BigDecimal(155.0),
                low = BigDecimal(145.0)
            )
        )

        assertEquals("Apple Inc", company.companyName)
        assertEquals("AAPL", company.stock.symbol)
        assertEquals(BigDecimal(150.0), company.stock.currentPrice)
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
            currentPrice = BigDecimal(150.0),
            high = BigDecimal(155.0),
            low = BigDecimal(145.0)
        )

        stock.currentPrice = BigDecimal(151.0)
        assertEquals(BigDecimal(151.0), stock.currentPrice)
    }

    @Test
    fun testStockHighLowUpdate() {
        val stock = Stock(symbol = "AAPL")
        stock.high = BigDecimal(160.0)
        stock.low = BigDecimal(140.0)

        assertEquals(BigDecimal(160.0), stock.high)
        assertEquals(BigDecimal(140.0), stock.low)
    }

    @Test
    fun testMultipleStockUpdates() {
        val stock = Stock(symbol = "AAPL")

        stock.currentPrice = BigDecimal(150.0)
        assertEquals(BigDecimal(150.0), stock.currentPrice)

        stock.currentPrice = BigDecimal(151.5)
        assertEquals(BigDecimal(151.5), stock.currentPrice)

        stock.high = BigDecimal(155.0)
        stock.low = BigDecimal(145.0)

        assertEquals(BigDecimal(155.0), stock.high)
        assertEquals(BigDecimal(145.0), stock.low)
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
        stock.currentPrice = BigDecimal(140.0)

        assertEquals("GOOG", stock.symbol)
        assertEquals(BigDecimal(140.0), stock.currentPrice)
    }
}
