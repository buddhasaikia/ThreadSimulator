package com.bs.threadsimulator.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bs.threadsimulator.data.DataRepository
import com.bs.threadsimulator.domain.FetchStockCurrentPriceUseCase
import com.bs.threadsimulator.domain.FetchStockHighLowUseCase
import com.bs.threadsimulator.domain.FetchStockPEUseCase
import com.bs.threadsimulator.model.Resource
import com.bs.threadsimulator.model.StateError
import com.bs.threadsimulator.model.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val fetchStockCurrentPriceUseCase: FetchStockCurrentPriceUseCase,
    private val fetchStockHighLowUseCase: FetchStockHighLowUseCase,
    private val fetchStockPEUseCase: FetchStockPEUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    init {
        start()
    }

    private fun start() {
        viewModelScope.launch {
            _uiState.update {
                val companyList = dataRepository.getCompanyList()
                it.copy(companyList = companyList)
            }
        }
        dataRepository.getCompanyList().forEach {
            viewModelScope.launch(Dispatchers.IO) {
                fetchCurrentPrice(it.stock.symbol)
            }
            viewModelScope.launch(Dispatchers.IO) {
                fetchHighLow(it.stock.symbol)
            }
            viewModelScope.launch(Dispatchers.IO) {
                fetchStockPE(it.stock.symbol)
            }
        }
        /*viewModelScope.launch(Dispatchers.IO) {
            fetchCurrentPrice("AAPL")
        }
        viewModelScope.launch(Dispatchers.IO) {
            fetchHighLow("AAPL")
        }
        viewModelScope.launch(Dispatchers.IO) {
            fetchCurrentPrice("MSFT")
        }
        viewModelScope.launch(Dispatchers.IO) {
            fetchHighLow("MSFT")
        }
        viewModelScope.launch(Dispatchers.IO) {
            fetchStockPE("MSFT")
        }
        viewModelScope.launch(Dispatchers.IO) {
            fetchStockPE("AAPL")
        }*/
    }

    private suspend fun fetchStockPE(symbol: String) {
        fetchStockPEUseCase.execute(symbol).collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.update {
                        it.copy(status = Status.Loading)
                    }
                }

                is Resource.Success -> {
                    _uiState.update {
                        it.copy(companyList = it.companyList.map { company ->
                            if (company.stock.symbol == resource.data?.stock?.symbol) {
                                resource.data
                            } else {
                                company.copy()
                            }
                        }, status = Status.Success)
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = StateError(resource.message, resource.throwable))
                    }
                }
            }
            println("buddha CurrentThread (PE ratio $symbol): ${Thread.currentThread().name}")
        }
    }

    private suspend fun fetchCurrentPrice(symbol: String) {
        fetchStockCurrentPriceUseCase.execute(symbol).collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.update {
                        it.copy(status = Status.Loading)
                    }
                }

                is Resource.Success -> {
                    _uiState.update {
                        it.copy(companyList = it.companyList.map { company ->
                            if (company.stock.symbol == resource.data?.symbol) {
                                company.copy(stock = resource.data)
                            } else {
                                company.copy()
                            }
                        }, status = Status.Success)
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = StateError(resource.message, resource.throwable))
                    }
                }
            }
            println("buddha CurrentThread (current price $symbol): ${Thread.currentThread().name}")
        }
    }

    private suspend fun fetchHighLow(symbol: String) {
        fetchStockHighLowUseCase.execute(symbol).collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.update {
                        it.copy(status = Status.Loading)
                    }
                }

                is Resource.Success -> {
                    _uiState.update {
                        it.copy(companyList = it.companyList.map { company ->
                            if (company.stock.symbol == resource.data?.symbol) {
                                company.copy(stock = resource.data)
                            } else {
                                company.copy()
                            }
                        }, status = Status.Success)
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = StateError(resource.message, resource.throwable))
                    }
                }
            }
            println("buddha CurrentThread (high low $symbol): ${Thread.currentThread().name}")
        }
    }
}