package com.bs.threadsimulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bs.threadsimulator.data.DataRepository
import com.bs.threadsimulator.domain.FetchStockInfoUseCase
import com.bs.threadsimulator.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val fetchStockInfoUseCase: FetchStockInfoUseCase
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
            startLiveTracking("AAPL")
        }
    }

    private suspend fun startLiveTracking(symbol: String) {
        fetchStockInfoUseCase.execute(symbol).collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    println("buddha loading")
                }

                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(companyList = state.companyList.map { company ->
                            if (company.stock.symbol == resource.data?.symbol) {
                                println("buddha match ${resource.data}")
                                company.copy(stock = resource.data)
                            } else {
                                println("buddha unmatch ${resource.data}")
                                company.copy()
                            }
                        })
                    }
                }

                is Resource.Error -> {
                    println("buddha error ${resource.message}")
                }
            }
            //println("buddha uiState.value.companyList ${uiState.value.companyList}")
        }
    }
}