package com.bs.threadsimulator.data.repository

import com.bs.threadsimulator.data.CompanyInfo
import com.bs.threadsimulator.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for stock data operations.
 *
 * Defines the contract for stock data fetching and management. Abstracts the underlying
 * implementation (e.g., [com.bs.threadsimulator.data.DataRepository]) to enable dependency
 * inversion and testability.
 *
 * All fetch operations are multi-threaded and emit continuous updates at configurable intervals.
 */
interface StockRepository {
    /**
     * Fetches and continuously updates the PE ratio for a given stock symbol.
     *
     * @param symbol The stock ticker symbol (e.g., "AAPL")
     * @return A Flow emitting Resource-wrapped CompanyInfo with updated PE values
     */
    suspend fun fetchStockPE(symbol: String): Flow<Resource<CompanyInfo>>

    /**
     * Fetches and continuously updates the current price for a given stock symbol.
     *
     * @param symbol The stock ticker symbol (e.g., "AAPL")
     * @return A Flow emitting Resource-wrapped CompanyInfo with updated current prices
     */
    suspend fun fetchStockCurrentPrice(symbol: String): Flow<Resource<CompanyInfo>>

    /**
     * Fetches and continuously updates high and low prices for a given stock symbol.
     *
     * @param symbol The stock ticker symbol (e.g., "AAPL")
     * @return A Flow emitting Resource-wrapped CompanyInfo with updated high/low prices
     */
    suspend fun fetchStockHighLow(symbol: String): Flow<Resource<CompanyInfo>>

    /**
     * Retrieves the current list of companies with their stock data.
     *
     * @return List of CompanyInfo objects containing stock and metrics data
     */
    fun getCompanyList(): List<CompanyInfo>

    /**
     * Sets the update interval for PE ratio updates.
     *
     * @param interval The delay in milliseconds between PE ratio updates
     */
    fun setUpdateIntervalPE(interval: Long)

    /**
     * Sets the update interval for high/low price updates.
     *
     * @param interval The delay in milliseconds between high/low updates
     */
    fun setUpdateIntervalHighLow(interval: Long)

    /**
     * Sets the update interval for current price updates.
     *
     * @param interval The delay in milliseconds between current price updates
     */
    fun setUpdateIntervalCurrentPrice(interval: Long)

    /**
     * Sets the size of the company list to simulate.
     *
     * @param interval The number of companies to include in simulated data
     */
    fun setListSize(interval: Long)
}
