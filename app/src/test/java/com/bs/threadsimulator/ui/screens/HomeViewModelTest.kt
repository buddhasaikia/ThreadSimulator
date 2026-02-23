package com.bs.threadsimulator.ui.screens

import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.data.DataRepository
import com.bs.threadsimulator.domain.FetchStockCurrentPriceUseCase
import com.bs.threadsimulator.domain.FetchStockHighLowUseCase
import com.bs.threadsimulator.domain.FetchStockPEUseCase
import com.bs.threadsimulator.domain.InitCompanyListUseCase
import com.bs.threadsimulator.domain.SetUpdateIntervalUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: HomeViewModel
    private lateinit var threadMonitor: ThreadMonitor
    private lateinit var dataRepository: DataRepository
    private lateinit var fetchCurrentPriceUseCase: FetchStockCurrentPriceUseCase
    private lateinit var fetchHighLowUseCase: FetchStockHighLowUseCase
    private lateinit var fetchPEUseCase: FetchStockPEUseCase
    private lateinit var initCompanyListUseCase: InitCompanyListUseCase
    private lateinit var setUpdateIntervalUseCase: SetUpdateIntervalUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        threadMonitor = ThreadMonitor()
        dataRepository = mockk(relaxed = true)
        fetchCurrentPriceUseCase = mockk(relaxed = true)
        fetchHighLowUseCase = mockk(relaxed = true)
        fetchPEUseCase = mockk(relaxed = true)
        initCompanyListUseCase = mockk(relaxed = true)
        setUpdateIntervalUseCase = mockk(relaxed = true)

        every { dataRepository.getCompanyList() } returns emptyList()

        viewModel = HomeViewModel(
            dataRepository = dataRepository,
            threadMonitor = threadMonitor,
            fetchStockCurrentPriceUseCase = fetchCurrentPriceUseCase,
            fetchStockHighLowUseCase = fetchHighLowUseCase,
            fetchStockPEUseCase = fetchPEUseCase,
            setUpdateIntervalUseCase = setUpdateIntervalUseCase,
            initCompanyListUseCase = initCompanyListUseCase
        )
    }

    @After
    fun tearDown() {
        viewModel.stop()
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialErrorMessageIsNull() {
        assertNull("errorMessage should be null initially", viewModel.errorMessage.value)
    }

    @Test
    fun testCompanyListInitiallyEmpty() {
        assertTrue("companyList should be empty when repository returns empty", viewModel.companyList.isEmpty())
    }

    @Test
    fun testThreadMetricsFlowIsAccessible() {
        assertNotNull("threadMetrics should not be null", viewModel.threadMetrics)
        assertNotNull("threadMetrics.value should not be null", viewModel.threadMetrics.value)
    }

    @Test
    fun testGetCompanyListCalledOnInit() {
        verify { dataRepository.getCompanyList() }
    }

    @Test
    fun testStopDoesNotThrow() {
        viewModel.stop()
        assertTrue("stop() executes without error", true)
    }

    @Test
    fun testSetUpdateIntervalDelegatesToUseCase() = runTest {
        coEvery { setUpdateIntervalUseCase.execute(any(), any()) } just Runs

        viewModel.setUpdateInterval("PE", 1500L)
        advanceUntilIdle()

        coVerify { setUpdateIntervalUseCase.execute("PE", 1500L) }
    }

    @Test
    fun testSetUpdateIntervalCurrentPrice() = runTest {
        coEvery { setUpdateIntervalUseCase.execute(any(), any()) } just Runs

        viewModel.setUpdateInterval("current_price", 2000L)
        advanceUntilIdle()

        coVerify { setUpdateIntervalUseCase.execute("current_price", 2000L) }
    }

    @Test
    fun testPopulateListCallsInitUseCase() = runTest {
        coEvery { initCompanyListUseCase.execute(any()) } just Runs

        viewModel.populateList(10)
        advanceUntilIdle()

        coVerify { initCompanyListUseCase.execute(10) }
    }

    @Test
    fun testPopulateListClearsAndRefreshesCompanyList() = runTest {
        every { dataRepository.getCompanyList() } returns emptyList()

        viewModel.populateList(5)
        advanceUntilIdle()

        assertTrue("companyList should reflect the repository data", viewModel.companyList.isEmpty())
    }

    @Test
    fun testErrorMessageCanBeUpdatedAndCleared() {
        viewModel.errorMessage.value = "Network error"
        assertEquals("Network error", viewModel.errorMessage.value)

        viewModel.errorMessage.value = null
        assertNull("errorMessage should be null after clearing", viewModel.errorMessage.value)
    }
}
