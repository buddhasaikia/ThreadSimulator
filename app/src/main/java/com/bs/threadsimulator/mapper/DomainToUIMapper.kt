package com.bs.threadsimulator.mapper

import com.bs.threadsimulator.data.CompanyInfo
import com.bs.threadsimulator.domain.model.CompanyData
import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.domain.model.Stock as DomainStock
import com.bs.threadsimulator.model.Stock as UIStock

/**
 * Extension function to convert a domain layer [CompanyData] to UI layer [Company].
 *
 * @receiver The CompanyData from the domain layer to transform
 * @return A new Company instance suitable for Compose state management
 */
fun CompanyData.toCompany(): Company =
    Company(
        companyName = companyName,
        categoryIndex = categoryIndex,
        peRatio = peRatio,
        previousClosingPrice = previousClosingPrice,
        threadName = threadName,
        stock =
            UIStock(
                stock.symbol,
                stock.openingPrice,
                stock.closingPrice,
                stock.low,
                stock.high,
                stock.currentPrice,
            ),
    )

/**
 * Extension function to convert a domain layer [DomainStock] to UI layer [UIStock].
 *
 * @receiver The Stock from the domain layer to transform
 * @return A new Stock instance for UI rendering
 */
fun DomainStock.toUIStock(): UIStock =
    UIStock(
        symbol = symbol,
        openingPrice = openingPrice,
        closingPrice = closingPrice,
        low = low,
        high = high,
        currentPrice = currentPrice,
    )

/**
 * Extension function to convert data layer [CompanyInfo] directly to UI layer [Company].
 *
 * Optimization for cases where we don't need to go through the domain layer.
 * This is used for initial list population where we don't need domain transformations.
 *
 * @receiver The CompanyInfo from the data layer to transform
 * @return A new Company instance suitable for Compose state management
 */
fun CompanyInfo.toCompany(): Company =
    Company(
        companyName = companyName,
        categoryIndex = categoryIndex,
        peRatio = peRatio,
        previousClosingPrice = previousClosingPrice,
        threadName = threadName,
        stock =
            UIStock(
                stock.symbol,
                stock.openingPrice,
                stock.closingPrice,
                stock.low,
                stock.high,
                stock.currentPrice,
            ),
    )
