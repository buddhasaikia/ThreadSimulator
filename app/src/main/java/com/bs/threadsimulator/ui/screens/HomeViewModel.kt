package com.bs.threadsimulator.ui.screens

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val fetchStockCurrentPriceUseCase: FetchStockCurrentPriceUseCase,
    private val fetchStockHighLowUseCase: FetchStockHighLowUseCase,
    private val fetchStockPEUseCase: FetchStockPEUseCase,
    private val setUpdateIntervalUseCase: SetUpdateIntervalUseCase,
    private val initCompanyListUseCase: InitCompanyListUseCase
) : ViewModel() {
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
            Log.i("ThreadSimulator", "Undelivered: $it")
        }
    )

    fun setUpdateInterval(name: String, interval: Long) {
        viewModelScope.launch {
            setUpdateIntervalUseCase.execute(name, interval)
        }
    }

    fun populateList(listSize: Int) {
        viewModelScope.launch {
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

                    else -> {}
                }
            }
        })
    }

    private fun fetchCurrentPrice(symbol: String) {
        jobs.add(viewModelScope.launch(Dispatchers.IO) {
            fetchStockCurrentPriceUseCase.execute(symbol).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        if (resource.data == null) return@collect
                        channel.send(resource.data)
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

                    else -> {}
                }
            }
        })
    }

    private fun initChannel(channel: ReceiveChannel<CompanyInfo>) {
        viewModelScope.launch(Dispatchers.Main) {
            for (companyInfo in channel) {
                val company = _companyList[companyInfo.id]
                with(company) {
                    threadName = companyInfo.threadName
                    peRatio = companyInfo.peRatio
                    stock.currentPrice = companyInfo.stock.currentPrice
                    stock.high = companyInfo.stock.high
                    stock.low = companyInfo.stock.low
                    threadName = companyInfo.threadName
                }
            }
        }
    }

    fun stop() {
        Log.i("ThreadSimulator", "Total jobs: ${jobs.count()}")
        jobs.forEach { it.cancel() }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
        channel.close()
    }
}