package com.bs.threadsimulator.domain

import com.bs.threadsimulator.data.DataRepository
import javax.inject.Inject

/**
 * Use case for fetching and updating stock PE ratio data.
 *
 * Encapsulates the business logic for retrieving continuous PE ratio updates for a given stock.
 * Coordinates with [DataRepository] to fetch data on the IO dispatcher.
 */
class FetchStockPEUseCase
    @Inject
    constructor(
        private val dataRepository: DataRepository,
    ) {
        /**
         * Executes the PE ratio fetch operation.
         *
         * @param symbol The stock ticker symbol (e.g., "AAPL")
         * @return A Flow emitting Resource-wrapped CompanyInfo with updated PE ratios
         */
        suspend fun execute(symbol: String) = dataRepository.fetchStockPE(symbol)
    }
