package com.bs.threadsimulator.domain

import com.bs.threadsimulator.data.DataRepository
import javax.inject.Inject

class FetchStockPEUseCase @Inject constructor(private val dataRepository: DataRepository) {
    suspend fun execute(symbol: String) = dataRepository.fetchStockPE(symbol)
}