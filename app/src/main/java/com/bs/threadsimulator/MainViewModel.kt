package com.bs.threadsimulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bs.threadsimulator.data.DataRepository
import com.bs.threadsimulator.data.MockDataSource
import com.bs.threadsimulator.domain.UpdateCurrentPriceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
    val dataRepository = DataRepository()

    init {
        start()
    }

    fun start() {
        viewModelScope.launch {
            _uiState.update {
                val companyList = MockDataSource().getCompanyList()
                it.copy(companyList = companyList)
            }
            startLiveTracking("AAPL")
        }
    }

    fun startLiveTracking(symbol: String) {
        viewModelScope.launch {
            UpdateCurrentPriceUseCase(dataRepository).execute(symbol).collect { resource ->
                _uiState.update { uiState ->
                    uiState.copy(companyList = uiState.companyList.map { company ->
                        if (company.stock.symbol == resource.data?.symbol) {
                            company.copy(stock = resource.data)
                        } else {
                            company
                        }
                    })
                }
            }
        }
    }
}