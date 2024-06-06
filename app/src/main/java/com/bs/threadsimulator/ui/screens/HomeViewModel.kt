package com.bs.threadsimulator.ui.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bs.threadsimulator.data.DataRepository
import com.bs.threadsimulator.domain.FetchStockCurrentPriceUseCase
import com.bs.threadsimulator.domain.FetchStockHighLowUseCase
import com.bs.threadsimulator.domain.FetchStockPEUseCase
import com.bs.threadsimulator.domain.SetUpdateIntervalUseCase
import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.Resource
import com.bs.threadsimulator.model.toCompany
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val fetchStockCurrentPriceUseCase: FetchStockCurrentPriceUseCase,
    private val fetchStockHighLowUseCase: FetchStockHighLowUseCase,
    private val fetchStockPEUseCase: FetchStockPEUseCase,
    private val setUpdateIntervalUseCase: SetUpdateIntervalUseCase
) : ViewModel() {
    private val _companyList = mutableStateListOf<Company>().apply {
        addAll(dataRepository.getCompanyList().map { it.toCompany() })
    }
    val companyList: List<Company>
        get() = _companyList

    private val jobs = mutableListOf<Job>()

    fun setUpdateInterval(name: String, interval: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            setUpdateIntervalUseCase.execute(name, interval)
        }
    }

    fun start() {
        dataRepository.getCompanyList().forEach {
            jobs.add(viewModelScope.launch(Dispatchers.Default) {
                fetchCurrentPrice(it.stock.symbol)
            })
            jobs.add(viewModelScope.launch(Dispatchers.IO) {
                fetchHighLow(it.stock.symbol)
            })
            jobs.add(viewModelScope.launch(Dispatchers.IO) {
                fetchStockPE(it.stock.symbol)
            })
        }
    }

    fun stop() {
        println("buddha Total jobs: ${jobs.count()}")
        jobs.forEach { it.cancel() }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }

    private suspend fun fetchStockPE(symbol: String) {
        fetchStockPEUseCase.execute(symbol).collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    _companyList.find { it.stock.symbol == symbol }?.let {
                        it.peRatio = resource.data?.peRatio ?: ""
                        it.threadName = resource.data?.threadName ?: ""
                    }
                }

                else -> {}
            }
            println("buddha CurrentThread (PE ratio $symbol): ${resource.data?.threadName ?: ""}")
        }
    }

    private suspend fun fetchCurrentPrice(symbol: String) {
        fetchStockCurrentPriceUseCase.execute(symbol).collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    _companyList.find { it.stock.symbol == symbol }?.let {
                        it.stock.currentPrice = resource.data?.stock?.currentPrice ?: 0.0
                        it.threadName = resource.data?.threadName ?: ""
                    }
                }

                else -> {}
            }
            println("buddha CurrentThread (current price $symbol): ${resource.data?.threadName ?: ""}")
        }
    }

    private suspend fun fetchHighLow(symbol: String) {
        fetchStockHighLowUseCase.execute(symbol).collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    _companyList.find { it.stock.symbol == symbol }?.let {
                        it.stock.high = resource.data?.stock?.high ?: 0.0
                        it.stock.low = resource.data?.stock?.low ?: 0.0
                        it.threadName = resource.data?.threadName ?: ""
                    }
                }

                else -> {}
            }
            println("buddha CurrentThread (high low $symbol): ${resource.data?.threadName ?: ""}")
        }
    }
}