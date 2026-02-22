package com.bs.threadsimulator.data

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MockDataSourceTest {

    private lateinit var mockDataSource: MockDataSource

    @Before
    fun setup() {
        mockDataSource = MockDataSource()
    }

    @Test
    fun testGetCompanyListReturnsValidList() {
        val companies = mockDataSource.getCompanyList()
        assertNotNull(companies)
    }

    @Test
    fun testCompanyListHasValidStructure() {
        val companies = mockDataSource.getCompanyList()
        companies.forEach { company ->
            assertNotNull("Company ID should not be null", company.id)
            assertNotNull("Company name should not be null", company.companyName)
            assertTrue("Company name should not be empty", company.companyName.isNotEmpty())
            assertNotNull("Stock info should not be null", company.stock)
            assertNotNull("Symbol should not be null", company.stock.symbol)
            assertTrue("Symbol should not be empty", company.stock.symbol.isNotEmpty())
        }
    }

    @Test
    fun testGenerateCompaniesAsyncReturnsCorrectNumber() = runBlocking {
        val count = 5
        val companies = mockDataSource.generateCompanies(count)
        assertEquals(count, companies.size)
    }

    @Test
    fun testGeneratedCompaniesHaveDistinctSymbols() = runBlocking {
        val companies = mockDataSource.generateCompanies(3)
        val symbols = companies.map { it.stock.symbol }
        assertEquals(3, symbols.distinct().size)
    }

    @Test
    fun testCompanyHasPERatio() {
        val companies = mockDataSource.getCompanyList()
        companies.forEach { company ->
            assertNotNull("PE Ratio should not be null", company.peRatio)
            assertTrue("PE Ratio should not be empty", company.peRatio.isNotEmpty())
        }
    }

    @Test
    fun testCompanyHasPreviousClosingPrice() {
        val companies = mockDataSource.getCompanyList()
        companies.forEach { company ->
            assertTrue("Previous closing price should be positive", company.previousClosingPrice > 0)
        }
    }

    @Test
    fun testStockPricesAreRealistic() {
        val companies = mockDataSource.getCompanyList()
        companies.forEach { company ->
            assertTrue("Opening price should be positive", company.stock.openingPrice.toDouble() > 0)
            assertTrue("Closing price should be positive", company.stock.closingPrice.toDouble() > 0)
            assertTrue("Current price should be positive", company.stock.currentPrice.toDouble() > 0)
            assertTrue("High should be positive", company.stock.high.toDouble() > 0)
            assertTrue("Low should be positive", company.stock.low.toDouble() > 0)
            assertTrue("High should be >= Low", company.stock.high >= company.stock.low)
        }
    }

    @Test
    fun testGenerateCompaniesWithLargerCount() = runBlocking {
        val companies = mockDataSource.generateCompanies(10)
        assertEquals(10, companies.size)
        companies.forEach { company ->
            assertTrue("Each company should have a valid structure", company.companyName.isNotEmpty())
        }
    }

    @Test
    fun testCompanyIndexIsUnique() = runBlocking {
        val companies = mockDataSource.generateCompanies(5)
        val indices = companies.map { it.id }
        assertEquals(5, indices.distinct().size)
    }

    @Test
    fun testCategoryIndexIsValid() {
        val companies = mockDataSource.getCompanyList()
        companies.forEach { company ->
            assertTrue("Category index should be in valid range", company.categoryIndex in 1..4)
        }
    }
}
