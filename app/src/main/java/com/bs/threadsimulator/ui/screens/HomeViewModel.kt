package com.bs.threadsimulator.ui.screens

import timber.log.Timber
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    threadMonitor: ThreadMonitor,
    private val fetchStockCurrentPriceUseCase: FetchStockCurrentPriceUseCase,
    private val fetchStockHighLowUseCase: FetchStockHighLowUseCase,
    private val fetchStockPEUseCase: FetchStockPEUseCase,
    private val setUpdateIntervalUseCase: SetUpdateIntervalUseCase,
    private val initCompanyListUseCase: InitCompanyListUseCase
) : ViewModel() {
    // Add StateFlow for thread metrics
    val threadMetrics: StateFlow<List<ThreadMetrics>> = threadMonitor.metrics
    // Expose error messages for UI feedback
    val errorMessage = mutableStateOf<String?>(null)
    private var currentThrottleStrategy = ThrottleStrategy.NORMAL
    private val _companyList = mutableStateListOf<Company>().apply {
        addAll(dataRepository.getCompanyList().map { it.toCompany() })
    }
    val companyList: List<Company>
        get() = _companyList
    private val jobs = mutableListOf<Job>()
    private val channel = Channel<CompanyInfo>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        capacity = 15000,
        onUndeliveredElement = {
            Timber.i("Undelivered: %s", it)
        }
    )

    // Automatically adjust based on list size
    private fun adjustThrottlingForListSize(listSize: Int) {
        val throttleMs = when {
            listSize > 100 -> ThrottleStrategy.RELAXED
            listSize > 50 -> ThrottleStrategy.NORMAL
            else -> ThrottleStrategy.RAPID
        }
        setUpdateThrottling(throttleMs)
    }

    private fun setUpdateThrottling(strategy: Long) {
        currentThrottleStrategy = strategy
        // Restart data collection with new throttling
        stop()
        start()
    }

    fun setUpdateInterval(name: String, interval: Long) {
        viewModelScope.launch {
            setUpdateIntervalUseCase.execute(name, interval)
        }
    }

    fun populateList(listSize: Int) {
        viewModelScope.launch {
            adjustThrottlingForListSize(listSize)
            initCompanyListUseCase.execute(listSize)
            _companyList.clear()
            _companyList.addAll(dataRepository.getCompanyList().map { it.toCompany() })
        }
    }

    fun start() {
        initChannel(channel)
        dataRepository.getCompanyList().forEach {
            fetchCurrentPrice(it.stock.symbol)
            fetchHighLow(it.stock.symbol)
            fetchStockPE(it.stock.symbol)
        }
    }

    private fun fetchStockPE(symbol: String) {
        jobs.add(viewModelScope.launch(Dispatchers.IO) {
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
        })
    }

    private fun fetchCurrentPrice(symbol: String) {
        jobs.add(viewModelScope.launch(Dispatchers.IO) {
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
        })
    }

    private fun fetchHighLow(symbol: String) {
        jobs.add(viewModelScope.launch(Dispatchers.IO) {
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
        })
    }

    private fun initChannel(channel: ReceiveChannel<CompanyInfo>) {
        viewModelScope.launch(Dispatchers.Main) {
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