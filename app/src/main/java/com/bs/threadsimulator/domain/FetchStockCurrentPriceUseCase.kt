package com.bs.threadsimulator.domain

import com.bs.threadsimulator.data.repository.StockRepository
import com.bs.threadsimulator.domain.model.CompanyData
import com.bs.threadsimulator.mapper.mapToDomainResource
import com.bs.threadsimulator.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching and updating stock current price data.
 *
 * Encapsulates the business logic for retrieving continuous current price updates for a given stock.
 * Coordinates with [StockRepository] to fetch data on the IO dispatcher and maps results to domain models.
 */
class FetchStockCurrentPriceUseCase
    @Inject
    constructor(
        private val stockRepository: StockRepository,
    ) {
        /**
         * Executes the current price fetch operation.
         *
         * @param symbol The stock ticker symbol (e.g., "AAPL")
         * @return A Flow emitting Resource-wrapped CompanyData with updated current prices
         */
        suspend fun execute(symbol: String): Flow<Resource<CompanyData>> =
            stockRepository
                .fetchStockCurrentPrice(symbol)
                .mapToDomainResource()
    }
