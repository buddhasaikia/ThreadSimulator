package com.bs.threadsimulator.ui.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.ChannelConfig
import com.bs.threadsimulator.common.ThreadMetrics
import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.common.ThrottleStrategy
import com.bs.threadsimulator.common.throttleUpdates
import com.bs.threadsimulator.data.repository.StockRepository
import com.bs.threadsimulator.domain.ExportMetricsUseCase
import com.bs.threadsimulator.domain.FetchStockCurrentPriceUseCase
import com.bs.threadsimulator.domain.FetchStockHighLowUseCase
import com.bs.threadsimulator.domain.FetchStockPEUseCase
import com.bs.threadsimulator.domain.InitCompanyListUseCase
import com.bs.threadsimulator.domain.SetUpdateIntervalUseCase
import com.bs.threadsimulator.domain.UpdateIntervalType
import com.bs.threadsimulator.domain.model.CompanyData
import com.bs.threadsimulator.mapper.toCompany
import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.ExportedMetrics
import com.bs.threadsimulator.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        private val stockRepository: StockRepository,
        private val threadMonitor: ThreadMonitor,
        private val appDispatchers: AppDispatchers,
        private val fetchStockCurrentPriceUseCase: FetchStockCurrentPriceUseCase,
        private val fetchStockHighLowUseCase: FetchStockHighLowUseCase,
        private val fetchStockPEUseCase: FetchStockPEUseCase,
        private val setUpdateIntervalUseCase: SetUpdateIntervalUseCase,
        private val initCompanyListUseCase: InitCompanyListUseCase,
        private val exportMetricsUseCase: ExportMetricsUseCase,
        private val channelConfig: ChannelConfig,
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
                addAll(stockRepository.getCompanyList().map { it.toCompany() })
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

        private val _droppedElementCount = MutableStateFlow(0L)

        /**
         * Observable count of elements that could not be delivered to receivers.
         *
         * Increments each time the channel's `onUndeliveredElement` callback is invoked
         * (for example, when a send fails due to channel closure, cancellation, or receiver
         * failure). It does **not** include elements silently discarded by
         * [BufferOverflow.DROP_OLDEST][kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST].
         * Useful for monitoring delivery failures and tuning [ChannelConfig.capacity].
         */
        val droppedElementCount: StateFlow<Long> = _droppedElementCount.asStateFlow()

        private val channel =
            Channel<CompanyData>(
                capacity = channelConfig.capacity,
                onBufferOverflow = channelConfig.onBufferOverflow,
                onUndeliveredElement = {
                    _droppedElementCount.update { it + 1 }
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
         * @param interval The update interval in milliseconds (or count for list_size)
         *
         * @see SetUpdateIntervalUseCase
         */
        @Deprecated("Use setUpdateInterval(UpdateIntervalType, Long) instead for type safety")
        fun setUpdateInterval(
            name: String,
            interval: Long,
        ) {
            val intervalType =
                when (name) {
                    "PE" -> UpdateIntervalType.PE
                    "current_price" -> UpdateIntervalType.CURRENT_PRICE
                    "high_low" -> UpdateIntervalType.HIGH_LOW
                    "list_size" -> UpdateIntervalType.LIST_SIZE
                    else -> return // Ignore unknown types
                }
            setUpdateInterval(intervalType, interval)
        }

        /**
         * Sets the update interval for a specific data type using type-safe enum.
         *
         * Executes on the IO dispatcher to prevent blocking. Changes take effect immediately.
         *
         * @param intervalType The type of configuration to update
         * @param interval The update interval in milliseconds (or count for LIST_SIZE)
         *
         * @see SetUpdateIntervalUseCase
         */
        fun setUpdateInterval(
            intervalType: UpdateIntervalType,
            interval: Long,
        ) {
            viewModelScope.launch {
                setUpdateIntervalUseCase.execute(intervalType, interval)
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
                _companyList.addAll(stockRepository.getCompanyList().map { it.toCompany() })
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
            stockRepository.getCompanyList().forEach {
                fetchCurrentPrice(it.stock.symbol)
                fetchHighLow(it.stock.symbol)
                fetchStockPE(it.stock.symbol)
            }
        }

        private fun fetchStockPE(symbol: String) {
            jobs.add(
                viewModelScope.launch(appDispatchers.ioDispatcher) {
                    fetchStockPEUseCase.execute(symbol).collect { resource ->
                        ensureActive()
                        when (resource) {
                            is Resource.Success -> {
                                if (resource.data == null) return@collect
                                threadMonitor.incrementQueueDepth()
                                try {
                                    channel.send(resource.data)
                                } catch (e: Exception) {
                                    threadMonitor.decrementQueueDepth()
                                    throw e
                                }
                            }

                            is Resource.Error -> {
                                Timber.e("PE fetch failed: %s", resource.message)
                                errorMessage.value = resource.message
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
                    fetchStockCurrentPriceUseCase
                        .execute(symbol)
                        .throttleUpdates(currentThrottleStrategy)
                        .collect { resource ->
                            ensureActive()
                            when (resource) {
                                is Resource.Success -> {
                                    if (resource.data == null) return@collect
                                    threadMonitor.incrementQueueDepth()
                                    try {
                                        channel.send(resource.data)
                                    } catch (e: Exception) {
                                        threadMonitor.decrementQueueDepth()
                                        throw e
                                    }
                                }

                                is Resource.Error -> {
                                    Timber.e("Current price fetch failed: %s", resource.message)
                                    errorMessage.value = resource.message
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
                        ensureActive()
                        when (resource) {
                            is Resource.Success -> {
                                if (resource.data == null) return@collect
                                threadMonitor.incrementQueueDepth()
                                try {
                                    channel.send(resource.data)
                                } catch (e: Exception) {
                                    threadMonitor.decrementQueueDepth()
                                    throw e
                                }
                            }

                            is Resource.Error -> {
                                Timber.e("High/Low fetch failed: %s", resource.message)
                                errorMessage.value = resource.message
                            }

                            else -> {}
                        }
                    }
                },
            )
        }

        private fun initChannel(channel: ReceiveChannel<CompanyData>) {
            viewModelScope.launch(appDispatchers.mainDispatcher) {
                try {
                    for (companyData in channel) {
                        threadMonitor.decrementQueueDepth()
                        ensureActive()
                        val company = _companyList.getOrNull(companyData.id) ?: continue
                        try {
                            company.updateFromDomain(companyData)
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
            jobs.clear()
        }

        /**
         * Exports collected metrics to the specified format.
         *
         * Launches the export operation on the IO dispatcher and updates error state.
         * On success, logs the file path and updates error state with success message.
         *
         * @param format Export format: "csv" or "json"
         */
        private fun exportMetrics(format: String) {
            viewModelScope.launch(appDispatchers.ioDispatcher) {
                when (val result = exportMetricsUseCase.execute(format)) {
                    is ExportedMetrics.Success -> {
                        errorMessage.value = "Exported to ${result.fileName}"
                        Timber.i("Metrics exported to %s: %s", format.uppercase(), result.filePath)
                    }
                    is ExportedMetrics.Error -> {
                        errorMessage.value = "Export failed: ${result.message}"
                        Timber.e("${format.uppercase()} export failed: %s", result.message)
                    }
                }
            }
        }

        /**
         * Exports collected metrics to CSV format.
         *
         * Convenience function that delegates to [exportMetrics] with "csv" format.
         */
        fun exportMetricsCSV() {
            exportMetrics("csv")
        }

        /**
         * Exports collected metrics to JSON format.
         *
         * Convenience function that delegates to [exportMetrics] with "json" format.
         */
        fun exportMetricsJSON() {
            exportMetrics("json")
        }

        override fun onCleared() {
            super.onCleared()
            stop()
            channel.close()
        }
    }
