package com.bs.threadsimulator.data

import com.bs.threadsimulator.data.Constants.updateIntervalCurrentPrice
import com.bs.threadsimulator.data.Constants.updateIntervalHighLow
import com.bs.threadsimulator.data.Constants.updateIntervalPE
import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.Resource
import com.bs.threadsimulator.model.Stock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal object Constants {
    var updateIntervalPE = 30L
    var updateIntervalHighLow = 50L
    var updateIntervalCurrentPrice = 10L
}

class DataRepository @Inject constructor(private val mockDataSource: MockDataSource) {
    private val startDelay = 0L

    fun setUpdateIntervalPE(interval: Long) {
        updateIntervalPE = interval
        println("updateIntervalPE $updateIntervalPE")
    }

    fun setUpdateIntervalHighLow(interval: Long) {
        updateIntervalHighLow = interval
        println("updateIntervalHighLow $updateIntervalHighLow")
    }

    fun setUpdateIntervalCurrentPrice(interval: Long) {
        updateIntervalCurrentPrice = interval
        println("updateIntervalCurrentPrice $updateIntervalCurrentPrice")
    }

    suspend fun fetchStockPE(symbol: String): Flow<Resource<Company>> {
        return flow {
            emit(Resource.Loading())
            delay(startDelay)
            while (true) {
                val company = getCompany(symbol)
                if (company != null) {
                    delay(updateIntervalPE)
                    company.peRatio = "${company.peRatio.toDouble().plus(1.0)}"
                    emit(Resource.Success(company))
                } else {
                    emit(Resource.Error(message = "Company not found"))
                }
            }
        }
    }

    suspend fun fetchStockCurrentPrice(symbol: String): Flow<Resource<Stock>> {
        return flow {
            emit(Resource.Loading())
            delay(startDelay)
            while (true) {
                val stock = getStock(symbol)
                if (stock != null) {
                    delay(updateIntervalCurrentPrice)
                    stock.currentPrice += 1
                    emit(Resource.Success(stock))
                } else {
                    emit(Resource.Error(message = "Stock not found"))
                }
            }
        }
    }

    suspend fun fetchStockHighLow(symbol: String): Flow<Resource<Stock>> {
        return flow {
            emit(Resource.Loading())
            delay(startDelay)
            while (true) {
                val stock = getStock(symbol)
                if (stock != null) {
                    delay(updateIntervalHighLow)
                    stock.high += 2
                    stock.low -= 1
                    emit(Resource.Success(stock))
                } else {
                    emit(Resource.Error(message = "Stock not found"))
                }
            }
        }
    }

    private fun getStock(symbol: String): Stock? {
        return mockDataSource.getCompanyList().find { it.stock.symbol == symbol }?.stock
    }

    private fun getCompany(symbol: String): Company? {
        return mockDataSource.getCompanyList().find { it.stock.symbol == symbol }
    }

    fun getCompanyList(): List<Company> {
        return mockDataSource.getCompanyList()
    }

}