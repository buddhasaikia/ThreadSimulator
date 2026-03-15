package com.bs.threadsimulator.feature.stockdata.ui.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bs.threadsimulator.common.ThreadMetrics
import com.bs.threadsimulator.common.ThrottleStrategy
import com.bs.threadsimulator.feature.stockdata.domain.FetchStockCurrentPriceUseCase
import com.bs.threadsimulator.feature.stockdata.domain.FetchStockHighLowUseCase
import com.bs.threadsimulator.feature.stockdata.domain.FetchStockPEUseCase
import com.bs.threadsimulator.feature.stockdata.domain.GetCompanyListUseCase
import com.bs.threadsimulator.feature.stockdata.domain.InitCompanyListUseCase
import com.bs.threadsimulator.feature.stockdata.domain.SetUpdateIntervalUseCase
import com.bs.threadsimulator.feature.stockdata.domain.UpdateIntervalType
import com.bs.threadsimulator.feature.stockdata.domain.model.CompanyData
import com.bs.threadsimulator.feature.stockdata.domain.service.StreamCoordinationService
import com.bs.threadsimulator.feature.stockdata.mapper.toCompany
import com.bs.threadsimulator.feature.stockdata.throttleUpdates
import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        private val fetchStockCurrentPriceUseCase: FetchStockCurrentPriceUseCase,
        private val fetchStockHighLowUseCase: FetchStockHighLowUseCase,
        private val fetchStockPEUseCase: FetchStockPEUseCase,
        private val setUpdateIntervalUseCase: SetUpdateIntervalUseCase,
        private val initCompanyListUseCase: InitCompanyListUseCase,
        private val getCompanyListUseCase: GetCompanyListUseCase,
        private val streamCoordinationService: StreamCoordinationService,
    ) : ViewModel() {
        /**
         * StateFlow of thread execution metrics.
         *
         * Emits real-time updates showing thread IDs, update counts, and average update times
         * for each data fetch operation (PE, CurrentPrice, HighLow).
         */
        val threadMetrics: StateFlow<List<ThreadMetrics>>
            get() = streamCoordinationService.monitor.metrics

        /**
         * Observable error message state.
         *
         * Set when any data fetch operation fails. Consumers can display this message to the user.
         */
        val errorMessage = mutableStateOf<String?>(null)

        private var currentThrottleStrategy = ThrottleStrategy.NORMAL
        private val _companyList =
            mutableStateListOf<Company>().apply {
                addAll(getCompanyListUseCase.execute().map { it.toCompany() })
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
        private var listenerJob: Job? = null

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
            streamCoordinationService.createCoordinationChannel { droppedData ->
                _droppedElementCount.value += 1
                Timber.w("Element dropped for: %s", droppedData.stock.symbol)
            }

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
                _droppedElementCount.value = 0L
                streamCoordinationService.monitor.clearMetrics()
                adjustThrottlingForListSize(listSize)
                initCompanyListUseCase.execute(listSize)
                _companyList.clear()
                _companyList.addAll(getCompanyListUseCase.execute().map { it.toCompany() })
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
            getCompanyListUseCase.execute().forEach {
                fetchCurrentPrice(it.stock.symbol)
                fetchHighLow(it.stock.symbol)
                fetchStockPE(it.stock.symbol)
            }
        }

        private fun fetchStockPE(symbol: String) {
            jobs.add(
                viewModelScope.launch(streamCoordinationService.dispatchers.ioDispatcher) {
                    fetchStockPEUseCase.execute(symbol).collect { resource ->
                        ensureActive()
                        when (resource) {
                            is Resource.Success -> {
                                val data: CompanyData = resource.data ?: return@collect
                                try {
                                    streamCoordinationService.sendWithTracking(
                                        channel,
                                        data,
                                    )
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to send PE data: %s", e.message)
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
                viewModelScope.launch(streamCoordinationService.dispatchers.ioDispatcher) {
                    fetchStockCurrentPriceUseCase
                        .execute(symbol)
                        .throttleUpdates(currentThrottleStrategy)
                        .collect { resource ->
                            ensureActive()
                            when (resource) {
                                is Resource.Success -> {
                                    val data: CompanyData = resource.data ?: return@collect
                                    try {
                                        streamCoordinationService.sendWithTracking(
                                            channel,
                                            data,
                                        )
                                    } catch (e: Exception) {
                                        Timber.e(
                                            e,
                                            "Failed to send current price data: %s",
                                            e.message,
                                        )
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
                viewModelScope.launch(streamCoordinationService.dispatchers.ioDispatcher) {
                    fetchStockHighLowUseCase.execute(symbol).collect { resource ->
                        ensureActive()
                        when (resource) {
                            is Resource.Success -> {
                                val data: CompanyData = resource.data ?: return@collect
                                try {
                                    streamCoordinationService.sendWithTracking(
                                        channel,
                                        data,
                                    )
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to send high/low data: %s", e.message)
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
            // Cancel previous listener if it exists (shouldn't happen, but be defensive)
            listenerJob?.cancel()

            listenerJob =
                viewModelScope.launch(streamCoordinationService.dispatchers.mainDispatcher) {
                    streamCoordinationService
                        .listenToChannel(
                            channel = channel,
                            onUpdate = { companyData ->
                                ensureActive()
                                val index =
                                    _companyList.indexOfFirst {
                                        it.stock.symbol == companyData.stock.symbol
                                    }
                                if (index >= 0) {
                                    _companyList[index] = companyData.toCompany()
                                }
                            },
                            onError = { message, exception ->
                                Timber.e(exception, "Channel error: %s", message)
                                errorMessage.value = message
                            },
                        ).collect { }
                }
        }

        /**
         * Stops all concurrent data fetch operations.
         *
         * Cancels all active jobs (both producers and channel listener), halting data collection.
         * Thread metrics up to cancellation are preserved and can still be observed.
         * Safe to call multiple times.
         *
         * @see start
         */
        fun stop() {
            Timber.i("Total producer jobs: %d", jobs.count())
            jobs.forEach { it.cancel() }
            jobs.clear()
            listenerJob?.cancel()
            listenerJob = null
        }

        /**
         * Exports collected metrics to the specified format.
         *
         * Currently a placeholder. Implement with MetricsExporter and ExportMetricsUseCase.
         *
         * @param format Export format: "csv" or "json"
         */
        private fun exportMetrics(format: String) {
            Timber.d("Export metrics ($format) - NOT YET IMPLEMENTED in feature module")
            errorMessage.value = "Export not yet available in this module"
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
