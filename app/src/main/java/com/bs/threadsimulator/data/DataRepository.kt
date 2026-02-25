package com.bs.threadsimulator.data

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.data.repository.StockRepository
import com.bs.threadsimulator.model.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Repository for managing stock data and simulating real-time updates across multiple threads.
 *
 * [DataRepository] provides a clean API for fetching stock information (PE ratio, current price,
 * high/low) with configurable update intervals. Each fetch operation runs on the IO dispatcher and
 * continuously emits updates, making it ideal for demonstrating multi-threaded data flow. All
 * threading metrics are automatically recorded via [ThreadMonitor].
 */
class DataRepository
    @Inject
    constructor(
        private val mockDataSource: MockDataSource,
        private val appDispatchers: AppDispatchers,
        private val threadMonitor: ThreadMonitor,
    ) : StockRepository {
        private val startDelay = 0L

        @Volatile
        private var updateIntervalPE = 1500L

        @Volatile
        private var updateIntervalHighLow = 1000L

        @Volatile
        private var updateIntervalCurrentPrice = 1000L

        @Volatile
        private var listSize = 5L

        /**
         * Sets the update interval for PE ratio updates.
         *
         * @param interval The delay in milliseconds between PE ratio updates
         */
        override fun setUpdateIntervalPE(interval: Long) {
            updateIntervalPE = interval
        }

        override fun getUpdateIntervalPE(): Long = updateIntervalPE

        /**
         * Sets the update interval for high/low price updates.
         *
         * @param interval The delay in milliseconds between high/low updates
         */
        override fun setUpdateIntervalHighLow(interval: Long) {
            updateIntervalHighLow = interval
        }

        override fun getUpdateIntervalHighLow(): Long = updateIntervalHighLow

        /**
         * Sets the update interval for current price updates.
         *
         * @param interval The delay in milliseconds between current price updates
         */
        override fun setUpdateIntervalCurrentPrice(interval: Long) {
            updateIntervalCurrentPrice = interval
        }

        override fun getUpdateIntervalCurrentPrice(): Long = updateIntervalCurrentPrice

        /**
         * Sets the size of the company list to simulate.
         *
         * This is NOT a time interval—it specifies the number of companies to generate.
         *
         * @param size The number of companies to include in simulated data (e.g., 5, 10, 50, 100)
         */
        override fun setListSize(size: Long) {
            listSize = size
        }

        override fun getListSize(): Long = listSize

        override suspend fun initCompanyList(size: Int) {
            mockDataSource.initCompanyList(size)
        }

        /**
         * Fetches and continuously updates the PE ratio for a given stock symbol.
         *
         * Runs on the IO dispatcher and emits updates at [updateIntervalPE] intervals.
         * Records each update with [ThreadMonitor] to track threading metrics.
         *
         * @param symbol The stock ticker symbol (e.g., "AAPL")
         * @return A Flow that emits [Resource.Loading], followed by continuous [Resource.Success]
         *         updates with incremented PE ratio values, or [Resource.Error] if symbol not found
         */
        override suspend fun fetchStockPE(symbol: String): Flow<Resource<CompanyInfo>> =
            withContext(appDispatchers.ioDispatcher) {
                flow {
                    emit(Resource.Loading())
                    delay(startDelay)
                    while (true) {
                        val startTime = System.nanoTime()
                        val index = getCompanyIndex(symbol)
                        if (index >= 0) {
                            delay(updateIntervalPE)
                            val companyInfo = mockDataSource.getCompanyList()[index]
                            val updated =
                                companyInfo.copy(
                                    peRatio = "${companyInfo.peRatio.toDouble().plus(1.0)}",
                                    threadName = Thread.currentThread().name,
                                )
                            val updateTime =
                                (System.nanoTime() - startTime) / 1_000_000 // Convert to ms
                            threadMonitor.recordUpdate("PE", updateTime)
                            emit(Resource.Success(updated))
                        } else {
                            emit(Resource.Error(message = "Company with symbol $symbol not found"))
                            break
                        }
                    }
                }.catch {
                    emit(Resource.Error(message = it.message ?: ""))
                }.flowOn(appDispatchers.ioDispatcher)
            }

        /**
         * Fetches and continuously updates the current price for a given stock symbol.
         *
         * Runs on the IO dispatcher and emits updates at [updateIntervalCurrentPrice] intervals.
         * Price is incremented by 1.0 with each update.
         *
         * @param symbol The stock ticker symbol (e.g., "AAPL")
         * @return A Flow that emits [Resource.Loading], followed by continuous [Resource.Success]
         *         updates with incremented current price values, or [Resource.Error] if symbol not found
         */
        override suspend fun fetchStockCurrentPrice(symbol: String): Flow<Resource<CompanyInfo>> =
            withContext(appDispatchers.ioDispatcher) {
                flow {
                    emit(Resource.Loading())
                    delay(startDelay)
                    while (true) {
                        val index = getCompanyIndex(symbol)
                        if (index >= 0) {
                            delay(updateIntervalCurrentPrice)
                            val companyInfo = mockDataSource.getCompanyList()[index]
                            val updated =
                                companyInfo.copy(
                                    stock =
                                        companyInfo.stock.copy(
                                            currentPrice =
                                                (companyInfo.stock.currentPrice + BigDecimal(1.0)).setScale(
                                                    2,
                                                    RoundingMode.HALF_UP,
                                                ),
                                        ),
                                    threadName = Thread.currentThread().name,
                                )
                            emit(Resource.Success(updated))
                        } else {
                            emit(Resource.Error(message = "Stock not found"))
                            break
                        }
                    }
                }.flowOn(appDispatchers.ioDispatcher)
            }

        /**
         * Fetches and continuously updates the high and low prices for a given stock symbol.
         *
         * Runs on the IO dispatcher and emits updates at [updateIntervalHighLow] intervals.
         * High price is incremented by 2.0 and low price by 1.0 with each update.
         *
         * @param symbol The stock ticker symbol (e.g., "AAPL")
         * @return A Flow that emits [Resource.Loading], followed by continuous [Resource.Success]
         *         updates with incremented high/low values, or [Resource.Error] if symbol not found
         */
        override suspend fun fetchStockHighLow(symbol: String): Flow<Resource<CompanyInfo>> =
            withContext(appDispatchers.ioDispatcher) {
                flow {
                    emit(Resource.Loading())
                    delay(startDelay)
                    while (true) {
                        val index = getCompanyIndex(symbol)
                        if (index >= 0) {
                            delay(updateIntervalHighLow)
                            val companyInfo = mockDataSource.getCompanyList()[index]
                            val updated =
                                companyInfo.copy(
                                    stock =
                                        companyInfo.stock.copy(
                                            high =
                                                (companyInfo.stock.high + BigDecimal(2.0)).setScale(
                                                    2,
                                                    RoundingMode.HALF_UP,
                                                ),
                                            low =
                                                (companyInfo.stock.low + BigDecimal(1.0)).setScale(
                                                    2,
                                                    RoundingMode.HALF_UP,
                                                ),
                                        ),
                                    threadName = Thread.currentThread().name,
                                )
                            emit(Resource.Success(updated))
                        } else {
                            emit(Resource.Error(message = "Stock not found"))
                            break
                        }
                    }
                }
            }

        private fun getCompanyIndex(symbol: String): Int = mockDataSource.getCompanyList().indexOfFirst { it.stock.symbol == symbol }

        /**
         * Returns the current list of companies with their stock data.
         *
         * @return List of CompanyInfo objects containing stock and metrics data
         */
        override fun getCompanyList(): List<CompanyInfo> = mockDataSource.getCompanyList()
    }
