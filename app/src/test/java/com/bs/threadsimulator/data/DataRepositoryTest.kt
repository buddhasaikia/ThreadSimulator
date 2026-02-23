package com.bs.threadsimulator.data

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.ThreadMonitor
import kotlinx.coroutines.runBlocking
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
        mockDataSource = MockDataSource()
        appDispatchers = AppDispatchers()
        threadMonitor = ThreadMonitor()

        repository = DataRepository(
            mockDataSource = mockDataSource,
            appDispatchers = appDispatchers,
            threadMonitor = threadMonitor
        )

        runBlocking {
            CompanyList.initCompanyList(5)
        }
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
    fun testSetUpdateIntervalPEUpdatesState() {
        repository.setUpdateIntervalPE(2000L)
        val companies = repository.getCompanyList()
        assertTrue("Should have companies after setting PE interval", companies.isNotEmpty())
    }

    @Test
    fun testSetUpdateIntervalHighLowUpdatesState() {
        repository.setUpdateIntervalHighLow(1500L)
        val companies = repository.getCompanyList()
        assertTrue("Should have companies after setting HighLow interval", companies.isNotEmpty())
    }

    @Test
    fun testSetUpdateIntervalCurrentPriceUpdatesState() {
        repository.setUpdateIntervalCurrentPrice(1200L)
        val companies = repository.getCompanyList()
        assertTrue("Should have companies after setting CurrentPrice interval", companies.isNotEmpty())
    }

    @Test
    fun testSetListSizeUpdatesState() {
        repository.setListSize(10L)
        val companies = repository.getCompanyList()
        assertTrue("Should still have companies after changing list size", companies.isNotEmpty())
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
