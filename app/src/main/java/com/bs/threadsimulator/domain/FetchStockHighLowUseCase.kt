package com.bs.threadsimulator.domain

import com.bs.threadsimulator.data.DataRepository
import javax.inject.Inject

/**
 * Use case for fetching and updating stock high and low price data.
 *
 * Encapsulates the business logic for retrieving continuous high/low price updates for a given stock.
 * Coordinates with [DataRepository] to fetch data on the IO dispatcher.
 */
class FetchStockHighLowUseCase
    @Inject
    constructor(private val dataRepository: DataRepository) {
        /**
         * Executes the high/low price fetch operation.
         *
         * @param symbol The stock ticker symbol (e.g., "AAPL")
         * @return A Flow emitting Resource-wrapped CompanyInfo with updated high/low prices
         */
        suspend fun execute(symbol: String) = dataRepository.fetchStockHighLow(symbol)
    }
