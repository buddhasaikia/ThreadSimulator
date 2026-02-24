package com.bs.threadsimulator.mapper

import com.bs.threadsimulator.data.CompanyInfo
import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.Stock as UIStock

/**
 * Extension function to convert data layer [CompanyInfo] directly to UI layer [Company].
 *
 * This is an optimization for cases where we don't need to go through the domain layer,
 * such as initial list population from the repository.
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
