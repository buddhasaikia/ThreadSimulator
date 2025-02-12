package com.bs.threadsimulator.data

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.data.Constants.listSize
import com.bs.threadsimulator.data.Constants.updateIntervalCurrentPrice
import com.bs.threadsimulator.data.Constants.updateIntervalHighLow
import com.bs.threadsimulator.data.Constants.updateIntervalPE
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

internal object Constants {
    var updateIntervalPE = 1500L
    var updateIntervalHighLow = 1000L
    var updateIntervalCurrentPrice = 1000L
    var listSize = 5L
}

class DataRepository @Inject constructor(
    private val mockDataSource: MockDataSource,
    private val appDispatchers: AppDispatchers,
    private val threadMonitor: ThreadMonitor
) {
    private val startDelay = 0L

    fun setUpdateIntervalPE(interval: Long) {
        updateIntervalPE = interval
    }

    fun setUpdateIntervalHighLow(interval: Long) {
        updateIntervalHighLow = interval
    }

    fun setUpdateIntervalCurrentPrice(interval: Long) {
        updateIntervalCurrentPrice = interval
    }

    fun setListSize(interval: Long) {
        listSize = interval
    }

    suspend fun fetchStockPE(symbol: String): Flow<Resource<CompanyInfo>> {
        return withContext(appDispatchers.ioDispatcher) {
            flow {
                emit(Resource.Loading())
                delay(startDelay)
                while (true) {
                    val startTime = System.nanoTime()
                    val index = getCompanyIndex(symbol)
                    if (index >= 0) {
                        delay(updateIntervalPE)
                        val companyInfo = mockDataSource.getCompanyList()[index]
                        companyInfo.apply {
                            peRatio = "${companyInfo.peRatio.toDouble().plus(1.0)}"
                            threadName = Thread.currentThread().name
                        }
                        val updateTime =
                            (System.nanoTime() - startTime) / 1_000_000 // Convert to ms
                        threadMonitor.recordUpdate("PE", updateTime)
                        emit(Resource.Success(companyInfo))
                    } else {
                        emit(Resource.Error(message = "Company not found"))
                    }
                }
            }.catch {
                emit(Resource.Error(message = it.message ?: ""))
            }.flowOn(appDispatchers.ioDispatcher)
        }
    }

    suspend fun fetchStockCurrentPrice(symbol: String): Flow<Resource<CompanyInfo>> {
        return withContext(appDispatchers.ioDispatcher) {
            flow {
                emit(Resource.Loading())
                delay(startDelay)
                while (true) {
                    val index = getCompanyIndex(symbol)
                    if (index >= 0) {
                        delay(updateIntervalCurrentPrice)
                        val companyInfo = mockDataSource.getCompanyList()[index]
                        companyInfo.stock.currentPrice =
                            (companyInfo.stock.currentPrice + BigDecimal(1.0)).setScale(
                                2,
                                RoundingMode.HALF_UP
                            )
                        companyInfo.threadName = Thread.currentThread().name
                        emit(Resource.Success(companyInfo))
                    } else {
                        emit(Resource.Error(message = "Stock not found"))
                    }
                }
            }.flowOn(appDispatchers.ioDispatcher)
        }
    }

    suspend fun fetchStockHighLow(symbol: String): Flow<Resource<CompanyInfo>> {
        return withContext(appDispatchers.ioDispatcher) {
            flow {
                emit(Resource.Loading())
                delay(startDelay)
                while (true) {
                    val index = getCompanyIndex(symbol)
                    if (index >= 0) {
                        delay(updateIntervalHighLow)
                        val companyInfo = mockDataSource.getCompanyList()[index]
                        companyInfo.stock.high =
                            (companyInfo.stock.high + BigDecimal(2.0)).setScale(
                                2,
                                RoundingMode.HALF_UP
                            )
                        companyInfo.stock.low = (companyInfo.stock.low + BigDecimal(1.0)).setScale(
                            2,
                            RoundingMode.HALF_UP
                        )
                        companyInfo.threadName = Thread.currentThread().name
                        emit(Resource.Success(companyInfo))
                    } else {
                        emit(Resource.Error(message = "Stock not found"))
                    }
                }
            }
        }
    }

    private fun getCompanyIndex(symbol: String): Int {
        return mockDataSource.getCompanyList().indexOfFirst { it.stock.symbol == symbol }
    }

    fun getCompanyList(): List<CompanyInfo> {
        return mockDataSource.getCompanyList()
    }

}