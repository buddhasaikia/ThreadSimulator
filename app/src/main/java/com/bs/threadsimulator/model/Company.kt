package com.bs.threadsimulator.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bs.threadsimulator.domain.model.CompanyData

/**
 * Composable-friendly data class representing a company with stock information.
 *
 * Uses Compose state properties (mutableStateOf) to enable reactive UI updates when values change.
 * Ideal for use in Jetpack Compose UI where state changes automatically trigger recomposition.
 *
 * @property companyName The name of the company
 * @property categoryIndex Index representing the company's category
 * @property peRatio The current Price-to-Earnings ratio
 * @property previousClosingPrice The stock price from the previous market close
 * @property stock Detailed stock price information (current, high, low, open, close)
 * @property threadName The name of the thread that last updated this company's data
 */
class Company(
    companyName: String = "",
    categoryIndex: Int = 0,
    peRatio: String = "",
    previousClosingPrice: Int = 0,
    stock: Stock = Stock(),
    threadName: String = "",
) {
    var companyName: String by mutableStateOf(companyName)
    var categoryIndex: Int by mutableIntStateOf(categoryIndex)
    var peRatio: String by mutableStateOf(peRatio)
    var previousClosingPrice: Int by mutableIntStateOf(previousClosingPrice)
    var stock: Stock by mutableStateOf(stock)
    var threadName by mutableStateOf(threadName)

    /**
     * Updates this Company with data from a domain model CompanyData.
     *
     * This batched update method is more efficient than updating fields individually,
     * as it performs all state updates in a single operation.
     *
     * @param data The domain model to update from
     */
    fun updateFromDomain(data: CompanyData) {
        this.threadName = data.threadName
        this.peRatio = data.peRatio
        this.stock =
            Stock(
                data.stock.symbol,
                data.stock.openingPrice,
                data.stock.closingPrice,
                data.stock.low,
                data.stock.high,
                data.stock.currentPrice,
            )
    }
}
