package com.bs.threadsimulator.domain

import com.bs.threadsimulator.data.repository.StockRepository
import com.bs.threadsimulator.domain.model.CompanyData
import com.bs.threadsimulator.mapper.mapToDomainResource
import com.bs.threadsimulator.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching and updating stock PE ratio data.
 *
 * Encapsulates the business logic for retrieving continuous PE ratio updates for a given stock.
 * Coordinates with [StockRepository] to fetch data on the IO dispatcher and maps results to domain models.
 */
class FetchStockPEUseCase
    @Inject
    constructor(
        private val stockRepository: StockRepository,
    ) {
        /**
         * Executes the PE ratio fetch operation.
         *
         * @param symbol The stock ticker symbol (e.g., "AAPL")
         * @return A Flow emitting Resource-wrapped CompanyData with updated PE ratios
         */
        suspend fun execute(symbol: String): Flow<Resource<CompanyData>> =
            stockRepository
                .fetchStockPE(symbol)
                .mapToDomainResource()
    }
