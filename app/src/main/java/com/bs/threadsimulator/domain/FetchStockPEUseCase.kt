package com.bs.threadsimulator.domain

import com.bs.threadsimulator.data.repository.StockRepository
import com.bs.threadsimulator.domain.model.CompanyData
import com.bs.threadsimulator.mapper.toDomainCompanyData
import com.bs.threadsimulator.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
            stockRepository.fetchStockPE(symbol).map { resource ->
                when (resource) {
                    is Resource.Success -> Resource.Success(resource.data?.toDomainCompanyData())
                    is Resource.Loading -> Resource.Loading()
                    is Resource.Error -> Resource.Error(resource.throwable, resource.message)
                }
            }
    }
