package com.bs.threadsimulator.feature.stockdata.domain

import com.bs.threadsimulator.feature.stockdata.data.repository.StockRepository
import com.bs.threadsimulator.feature.stockdata.domain.model.CompanyData
import com.bs.threadsimulator.feature.stockdata.mapper.toDomainCompanyData
import javax.inject.Inject

/**
 * Use case for retrieving the current list of companies.
 *
 * Provides a domain-layer abstraction for fetching the company list,
 * allowing the UI to depend on the domain layer rather than directly accessing the repository.
 * Maps data layer models to domain models.
 */
class GetCompanyListUseCase
    @Inject
    constructor(
        private val stockRepository: StockRepository,
    ) {
        /**
         * Retrieves the current list of companies.
         *
         * @return A list of CompanyData domain models
         */
        fun execute(): List<CompanyData> = stockRepository.getCompanyList().map { it.toDomainCompanyData() }
    }
