package com.bs.threadsimulator.mapper

import com.bs.threadsimulator.data.CompanyInfo
import com.bs.threadsimulator.data.StockInfo
import com.bs.threadsimulator.domain.model.CompanyData
import com.bs.threadsimulator.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.bs.threadsimulator.domain.model.Stock as DomainStock

/**
 * Extension function to convert a data layer [CompanyInfo] to domain layer [CompanyData].
 *
 * @receiver The CompanyInfo from the data layer to transform
 * @return A new CompanyData instance with the same semantic information
 */
fun CompanyInfo.toDomainCompanyData(): CompanyData =
    CompanyData(
        id = id,
        companyName = companyName,
        categoryIndex = categoryIndex,
        peRatio = peRatio,
        previousClosingPrice = previousClosingPrice,
        threadName = threadName,
        stock = stock.toDomainStock(),
    )

/**
 * Extension function to convert a data layer [StockInfo] to domain layer [DomainStock].
 *
 * @receiver The StockInfo from the data layer to transform
 * @return A new Stock instance with the same semantic information
 */
fun StockInfo.toDomainStock(): DomainStock =
    DomainStock(
        symbol = symbol,
        openingPrice = openingPrice,
        closingPrice = closingPrice,
        low = low,
        high = high,
        currentPrice = currentPrice,
    )

/**
 * Extension function to transform a Flow of Resource<CompanyInfo> to Flow of Resource<CompanyData>.
 *
 * Maps successful results through the data-to-domain mapper while preserving Loading and Error states.
 * This is the standard transformation pattern for repository flows in use cases.
 *
 * @receiver The Flow<Resource<CompanyInfo>> from the repository
 * @return A Flow<Resource<CompanyData>> with domain models
 */
fun Flow<Resource<CompanyInfo>>.mapToDomainResource(): Flow<Resource<CompanyData>> =
    map { resource ->
        when (resource) {
            is Resource.Success -> Resource.Success(resource.data?.toDomainCompanyData())
            is Resource.Loading -> Resource.Loading()
            is Resource.Error -> Resource.Error(resource.throwable, resource.message)
        }
    }
