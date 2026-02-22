package com.bs.threadsimulator.data

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.ThreadMonitor
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
    }

    @Test
    fun testGetCompanyList() {
        val companies = repository.getCompanyList()
        assertNotNull(companies)
        assertTrue("Should have at least some companies", companies.size >= 0)
    }

    @Test
    fun testSetUpdateIntervalPE() {
        repository.setUpdateIntervalPE(2000L)
    }

    @Test
    fun testSetUpdateIntervalHighLow() {
        repository.setUpdateIntervalHighLow(1500L)
    }

    @Test
    fun testSetUpdateIntervalCurrentPrice() {
        repository.setUpdateIntervalCurrentPrice(1200L)
    }

    @Test
    fun testSetListSize() {
        repository.setListSize(10L)
    }

    @Test
    fun testRepositoryInitialization() {
        assertNotNull(repository)
    }
}
