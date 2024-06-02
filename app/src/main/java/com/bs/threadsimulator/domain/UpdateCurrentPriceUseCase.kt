package com.bs.threadsimulator.domain

import com.bs.threadsimulator.data.DataRepository

class UpdateCurrentPriceUseCase(private val dataRepository: DataRepository) {
    suspend fun execute(symbol: String) = dataRepository.fetchLiveStockStatus(symbol)
}