package com.bs.threadsimulator.domain

import com.bs.threadsimulator.data.DataRepository
import javax.inject.Inject

/**
 * Use case for fetching and updating stock current price data.
 *
 * Encapsulates the business logic for retrieving continuous current price updates for a given stock.
 * Coordinates with [DataRepository] to fetch data on the IO dispatcher.
 */
class FetchStockCurrentPriceUseCase
    @Inject
    constructor(
        private val dataRepository: DataRepository,
    ) {
        /**
         * Executes the current price fetch operation.
         *
         * @param symbol The stock ticker symbol (e.g., "AAPL")
         * @return A Flow emitting Resource-wrapped CompanyInfo with updated current prices
         */
        suspend fun execute(symbol: String) = dataRepository.fetchStockCurrentPrice(symbol)
    }
