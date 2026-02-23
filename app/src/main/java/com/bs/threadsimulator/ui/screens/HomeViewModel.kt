package com.bs.threadsimulator.ui.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.ThreadMetrics
import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.common.ThrottleStrategy
import com.bs.threadsimulator.common.throttleUpdates
import com.bs.threadsimulator.data.CompanyInfo
import com.bs.threadsimulator.data.DataRepository
import com.bs.threadsimulator.domain.FetchStockCurrentPriceUseCase
import com.bs.threadsimulator.domain.FetchStockHighLowUseCase
import com.bs.threadsimulator.domain.FetchStockPEUseCase
import com.bs.threadsimulator.domain.InitCompanyListUseCase
import com.bs.threadsimulator.domain.SetUpdateIntervalUseCase
import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.Resource
import com.bs.threadsimulator.model.toCompany
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for managing thread simulation state and multi-threaded data flows.
 *
 * [HomeViewModel] coordinates the execution of concurrent stock data fetch operations,
 * manages throttling strategies based on list size, and aggregates thread execution metrics.
 * It demonstrates real-time multi-threaded data collection via channels and provides
 * comprehensive error handling.
 *
 * Thread Safety: Uses Hilt injection and coroutines for thread-safe state management.
 */
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val dataRepository: DataRepository,
        private val threadMonitor: ThreadMonitor,
        private val appDispatchers: AppDispatchers,
        private val fetchStockCurrentPriceUseCase: FetchStockCurrentPriceUseCase,
        private val fetchStockHighLowUseCase: FetchStockHighLowUseCase,
        private val fetchStockPEUseCase: FetchStockPEUseCase,
        private val setUpdateIntervalUseCase: SetUpdateIntervalUseCase,
        private val initCompanyListUseCase: InitCompanyListUseCase,
    ) : ViewModel() {
        /**
         * StateFlow of thread execution metrics.
         *
         * Emits real-time updates showing thread IDs, update counts, and average update times
         * for each data fetch operation (PE, CurrentPrice, HighLow).
         */
        val threadMetrics: StateFlow<List<ThreadMetrics>> = threadMonitor.metrics

        /**
         * Observable error message state.
         *
         * Set when any data fetch operation fails. Consumers can display this message to the user.
         */
        val errorMessage = mutableStateOf<String?>(null)

        private var currentThrottleStrategy = ThrottleStrategy.NORMAL
        private val _companyList =
            mutableStateListOf<Company>().apply {
                addAll(dataRepository.getCompanyList().map { it.toCompany() })
            }

        /**
         * The current list of companies with their stock data.
         *
         * Updated in real-time as data arrives from the concurrent fetch operations.
         * Read-only from outside; mutations happen internally via channel processing.
         */
        val companyList: List<Company>
            get() = _companyList
        private val jobs = mutableListOf<Job>()
        private val channel =
            Channel<CompanyInfo>(
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
                capacity = 15000,
                onUndeliveredElement = {
                    Timber.i("Undelivered: %s", it)
                },
            )

        private fun adjustThrottlingForListSize(listSize: Int) {
            val throttleMs =
                when {
                    listSize > 100 -> ThrottleStrategy.RELAXED
                    listSize > 50 -> ThrottleStrategy.NORMAL
                    else -> ThrottleStrategy.RAPID
                }
            setUpdateThrottling(throttleMs)
        }

        private fun setUpdateThrottling(strategy: Long) {
            currentThrottleStrategy = strategy
            stop()
            start()
        }

        /**
         * Sets the update interval for a specific data type.
         *
         * Executes on the IO dispatcher to prevent blocking. Changes take effect immediately.
         *
         * @param name The type of update: "PE", "current_price", "high_low", or "list_size"
         * @param interval The update interval in milliseconds
         *
         * @see SetUpdateIntervalUseCase
         */
        fun setUpdateInterval(
            name: String,
            interval: Long,
        ) {
            viewModelScope.launch {
                setUpdateIntervalUseCase.execute(name, interval)
            }
        }

        /**
         * Populates the company list with the specified number of companies.
         *
         * Automatically adjusts throttling strategy based on list size for optimal performance.
         * Clears accumulated metrics before starting a new simulation.
         * Updates the UI state with the new list of companies.
         *
         * @param listSize The number of companies to simulate
         *
         * @see InitCompanyListUseCase
         * @see adjustThrottlingForListSize
         */
        fun populateList(listSize: Int) {
            viewModelScope.launch {
                // Ensure all previous jobs are cancelled so they can't emit metrics into the new run.
                stop()
                threadMonitor.clearMetrics()
                adjustThrottlingForListSize(listSize)
                initCompanyListUseCase.execute(listSize)
                _companyList.clear()
                _companyList.addAll(dataRepository.getCompanyList().map { it.toCompany() })
            }
        }

        /**
         * Starts all concurrent data fetch operations.
         *
         * Initializes the channel listener and launches three concurrent collection tasks
         * (PE, CurrentPrice, HighLow) for each company in the list. Each task runs on the IO dispatcher.
         *
         * Thread Safety: Safe to call multiple times; subsequent calls will launch additional jobs.
         * Call [stop] before starting again to avoid duplicate jobs.
         *
         * @see stop
         */
        fun start() {
            initChannel(channel)
            dataRepository.getCompanyList().forEach {
                fetchCurrentPrice(it.stock.symbol)
                fetchHighLow(it.stock.symbol)
                fetchStockPE(it.stock.symbol)
            }
        }

        private fun fetchStockPE(symbol: String) {
            jobs.add(
                viewModelScope.launch(appDispatchers.ioDispatcher) {
                    fetchStockPEUseCase.execute(symbol).collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                if (resource.data == null) return@collect
                                channel.send(resource.data)
                            }

                            is Resource.Error -> {
                                Timber.e("PE fetch failed: %s", resource.message)
                                errorMessage.value = resource.message ?: "Failed to fetch PE"
                            }

                            else -> {}
                        }
                    }
                },
            )
        }

        private fun fetchCurrentPrice(symbol: String) {
            jobs.add(
                viewModelScope.launch(appDispatchers.ioDispatcher) {
                    fetchStockCurrentPriceUseCase.execute(symbol)
                        .throttleUpdates(currentThrottleStrategy)
                        .collect { resource ->
                            when (resource) {
                                is Resource.Success -> {
                                    if (resource.data == null) return@collect
                                    channel.send(resource.data)
                                }

                                is Resource.Error -> {
                                    Timber.e("Current price fetch failed: %s", resource.message)
                                    errorMessage.value = resource.message ?: "Failed to fetch current price"
                                }

                                else -> {}
                            }
                        }
                },
            )
        }

        private fun fetchHighLow(symbol: String) {
            jobs.add(
                viewModelScope.launch(appDispatchers.ioDispatcher) {
                    fetchStockHighLowUseCase.execute(symbol).collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                if (resource.data == null) return@collect
                                channel.send(resource.data)
                            }

                            is Resource.Error -> {
                                Timber.e("High/Low fetch failed: %s", resource.message)
                                errorMessage.value = resource.message ?: "Failed to fetch high/low"
                            }

                            else -> {}
                        }
                    }
                },
            )
        }

        private fun initChannel(channel: ReceiveChannel<CompanyInfo>) {
            viewModelScope.launch(appDispatchers.mainDispatcher) {
                try {
                    for (companyInfo in channel) {
                        val company = _companyList.getOrNull(companyInfo.id) ?: continue
                        try {
                            with(company) {
                                threadName = companyInfo.threadName
                                peRatio = companyInfo.peRatio
                                stock.currentPrice = companyInfo.stock.currentPrice
                                stock.high = companyInfo.stock.high
                                stock.low = companyInfo.stock.low
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to apply update: %s", e.message)
                            errorMessage.value = "Failed to update UI"
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Channel processing failed: %s", e.message)
                    errorMessage.value = "Internal processing error"
                }
            }
        }

        /**
         * Stops all concurrent data fetch operations.
         *
         * Cancels all active jobs, halting data collection. Thread metrics up to cancellation
         * are preserved and can still be observed. Safe to call multiple times.
         *
         * @see start
         */
        fun stop() {
            Timber.i("Total jobs: %d", jobs.count())
            jobs.forEach { it.cancel() }
        }

        override fun onCleared() {
            super.onCleared()
            stop()
            channel.close()
        }
    }
