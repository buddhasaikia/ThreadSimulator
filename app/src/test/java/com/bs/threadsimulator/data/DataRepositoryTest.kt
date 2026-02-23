package com.bs.threadsimulator.data

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.ThreadMonitor
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class DataRepositoryTest {

    private lateinit var repository: DataRepository
    private lateinit var mockDataSource: MockDataSource
    private lateinit var appDispatchers: AppDispatchers
    private lateinit var threadMonitor: ThreadMonitor

    @Before
    fun setup() {
        mockDataSource = MockDataSource(AppDispatchers())
        appDispatchers = AppDispatchers()
        threadMonitor = ThreadMonitor()

        repository = DataRepository(
            mockDataSource = mockDataSource,
            appDispatchers = appDispatchers,
            threadMonitor = threadMonitor
        )

        runBlocking {
            CompanyList.initCompanyList(5, AppDispatchers())
        }
    }

    @After
    fun tearDown() {
        // Reset Constants to defaults to avoid test interference
        Constants.updateIntervalPE = 1500L
        Constants.updateIntervalHighLow = 1000L
        Constants.updateIntervalCurrentPrice = 1000L
        Constants.listSize = 5L
    }

    @Test
    fun testGetCompanyListReturnsNonEmptyList() {
        val companies = repository.getCompanyList()
        assertNotNull(companies)
        assertTrue("Should have initialized companies", companies.isNotEmpty())
        assertEquals(5, companies.size)
    }

    @Test
    fun testCompanyListHasValidSymbols() {
        val companies = repository.getCompanyList()
        companies.forEach { company ->
            assertNotNull("Symbol should not be null", company.stock.symbol)
            assertTrue("Symbol should not be empty", company.stock.symbol.isNotEmpty())
        }
    }

    @Test
    fun testSetUpdateIntervalPEUpdatesConstant() {
        repository.setUpdateIntervalPE(2000L)
        assertEquals("PE interval should be updated to 2000", 2000L, Constants.updateIntervalPE)
    }

    @Test
    fun testSetUpdateIntervalHighLowUpdatesConstant() {
        repository.setUpdateIntervalHighLow(1500L)
        assertEquals("HighLow interval should be updated to 1500", 1500L, Constants.updateIntervalHighLow)
    }

    @Test
    fun testSetUpdateIntervalCurrentPriceUpdatesConstant() {
        repository.setUpdateIntervalCurrentPrice(1200L)
        assertEquals("CurrentPrice interval should be updated to 1200", 1200L, Constants.updateIntervalCurrentPrice)
    }

    @Test
    fun testSetListSizeUpdatesConstant() {
        repository.setListSize(10L)
        assertEquals("List size should be updated to 10", 10L, Constants.listSize)
    }

    @Test
    fun testRepositoryInitialization() {
        assertNotNull(repository)
        assertNotNull(repository.getCompanyList())
    }

    @Test
    fun testCompanyListHasConsistentData() {
        val companies = repository.getCompanyList()
        companies.forEach { company ->
            assertNotNull("Company info should not be null", company)
            assertNotNull("Stock should not be null", company.stock)
            assertTrue("Opening price should be positive", company.stock.openingPrice.toDouble() > 0)
            assertTrue("Closing price should be positive", company.stock.closingPrice.toDouble() > 0)
        }
    }
}
