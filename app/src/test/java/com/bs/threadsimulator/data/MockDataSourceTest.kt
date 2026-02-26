package com.bs.threadsimulator.data

import com.bs.threadsimulator.common.TestAppDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockDataSourceTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockDataSource: MockDataSource

    @Before
    fun setup() {
        mockDataSource = MockDataSource(TestAppDispatchers(testDispatcher))
        runTest(testDispatcher) {
            mockDataSource.initCompanyList(5)
        }
    }

    @Test
    fun testGetCompanyListReturnsNonEmptyData() {
        val companies = mockDataSource.getCompanyList()
        assertNotNull(companies)
        assertTrue("Should have initialized company data", companies.isNotEmpty())
    }

    @Test
    fun testCompanyListHasValidStructure() {
        val companies = mockDataSource.getCompanyList()
        assertTrue("Should have at least one company", companies.isNotEmpty())
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
    fun testGenerateCompaniesReturnsCorrectCount() =
        runTest(testDispatcher) {
            val count = 5
            val companies = mockDataSource.generateCompanies(count)
            assertEquals(count, companies.size)
        }

    @Test
    fun testGeneratedCompaniesHaveValidSymbolFormat() =
        runTest(testDispatcher) {
            val companies = mockDataSource.generateCompanies(5)
            companies.forEachIndexed { index, company ->
                val symbol = company.stock.symbol
                assertTrue("Symbol should not be empty", symbol.isNotEmpty())
                // Symbol format: <LETTERS><4-digit-index>, e.g. "AAPL0000"
                assertTrue("Symbol should end with 4-digit index", symbol.matches(Regex(".*\\d{4}$")))
                assertTrue(
                    "Symbol should contain the padded index",
                    symbol.endsWith(index.toString().padStart(4, '0')),
                )
            }
        }

    @Test
    fun testGeneratedCompaniesHaveValidPrices() =
        runTest(testDispatcher) {
            val companies = mockDataSource.generateCompanies(5)
            companies.forEach { company ->
                val high = company.stock.high.toDouble()
                val low = company.stock.low.toDouble()
                val currentPrice = company.stock.currentPrice.toDouble()
                val openingPrice = company.stock.openingPrice.toDouble()
                val closingPrice = company.stock.closingPrice.toDouble()

                assertTrue("Opening price should be positive", openingPrice > 0)
                assertTrue("Closing price should be positive", closingPrice > 0)
                assertTrue("High should be >= Low", high >= low)
                assertTrue("Current price should be within [low, high]", currentPrice >= low && currentPrice <= high)
                // High must be at least as large as max(opening, closing)
                val maxPrice = maxOf(openingPrice, closingPrice)
                assertTrue("High should be >= max(opening, closing)", high >= maxPrice * 0.99)
                // Low must be at most as small as min(opening, closing)
                val minPrice = minOf(openingPrice, closingPrice)
                assertTrue("Low should be <= min(opening, closing)", low <= minPrice * 1.01)
            }
        }

    @Test
    fun testGeneratedCompaniesHaveValidPERatio() =
        runTest(testDispatcher) {
            val companies = mockDataSource.generateCompanies(5)
            companies.forEach { company ->
                val pe = company.peRatio.toDouble()
                assertTrue("PE ratio should be >= 10.0", pe >= 10.0)
                assertTrue("PE ratio should be <= 60.0", pe <= 60.0)
            }
        }

    @Test
    fun testCompanyListHasPreviousClosingPrice() {
        val companies = mockDataSource.getCompanyList()
        assertTrue("List should not be empty", companies.isNotEmpty())
        companies.forEach { company ->
            assertTrue("Previous closing price should be positive", company.previousClosingPrice > 0)
        }
    }

    @Test
    fun testGenerateCompaniesWithLargeCount() =
        runTest(testDispatcher) {
            val companies = mockDataSource.generateCompanies(10)
            assertEquals(10, companies.size)
            companies.forEach { company ->
                assertTrue("Each company should have a valid structure", company.companyName.isNotEmpty())
            }
        }

    @Test
    fun testGeneratedCompaniesHaveUniqueIndices() =
        runTest(testDispatcher) {
            val companies = mockDataSource.generateCompanies(5)
            val indices = companies.map { it.id }
            assertEquals("All company IDs should be unique", 5, indices.distinct().size)
        }

    @Test
    fun testCategoryIndexIsValid() {
        val companies = mockDataSource.getCompanyList()
        assertTrue("List should not be empty", companies.isNotEmpty())
        companies.forEach { company ->
            assertTrue("Category index should be in valid range", company.categoryIndex in 1..4)
        }
    }
}
