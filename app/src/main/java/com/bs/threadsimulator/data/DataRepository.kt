package com.bs.threadsimulator.data

import com.bs.threadsimulator.data.Constants.updateIntervalCurrentPrice
import com.bs.threadsimulator.data.Constants.updateIntervalHighLow
import com.bs.threadsimulator.data.Constants.updateIntervalPE
import com.bs.threadsimulator.model.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal object Constants {
    var updateIntervalPE = 1500L
    var updateIntervalHighLow = 1000L
    var updateIntervalCurrentPrice = 1000L
}

class DataRepository @Inject constructor(private val mockDataSource: MockDataSource) {
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

    suspend fun fetchStockPE(symbol: String): Flow<Resource<CompanyInfo>> {
        return flow {
            emit(Resource.Loading())
            delay(startDelay)
            while (true) {
                val companyInfo = getCompany(symbol)
                if (companyInfo != null) {
                    delay(updateIntervalPE)
                    companyInfo.peRatio = "${companyInfo.peRatio.toDouble().plus(1.0)}"
                    companyInfo.threadName = Thread.currentThread().name
                    emit(Resource.Success(companyInfo))
                } else {
                    emit(Resource.Error(message = "Company not found"))
                }
            }
        }
    }

    suspend fun fetchStockCurrentPrice(symbol: String): Flow<Resource<CompanyInfo>> {
        return flow {
            emit(Resource.Loading())
            delay(startDelay)
            while (true) {
                val companyInfo = getCompany(symbol)
                if (companyInfo != null) {
                    delay(updateIntervalCurrentPrice)
                    companyInfo.stock.currentPrice += 1
                    companyInfo.threadName = Thread.currentThread().name
                    emit(Resource.Success(companyInfo))
                } else {
                    emit(Resource.Error(message = "Stock not found"))
                }
            }
        }
    }

    suspend fun fetchStockHighLow(symbol: String): Flow<Resource<CompanyInfo>> {
        return flow {
            emit(Resource.Loading())
            delay(startDelay)
            while (true) {
                val companyInfo = getCompany(symbol)
                if (companyInfo != null) {
                    delay(updateIntervalHighLow)
                    companyInfo.stock.high += 2
                    companyInfo.stock.low -= 1
                    companyInfo.threadName = Thread.currentThread().name
                    emit(Resource.Success(companyInfo))
                } else {
                    emit(Resource.Error(message = "Stock not found"))
                }
            }
        }
    }

    private fun getCompany(symbol: String): CompanyInfo? {
        return mockDataSource.getCompanyList().find { it.stock.symbol == symbol }
    }

    fun getCompanyList(): List<CompanyInfo> {
        return mockDataSource.getCompanyList()
    }

}