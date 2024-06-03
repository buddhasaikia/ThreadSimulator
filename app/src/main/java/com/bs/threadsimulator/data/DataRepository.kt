package com.bs.threadsimulator.data

import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.Resource
import com.bs.threadsimulator.model.Stock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DataRepository @Inject constructor(private val mockDataSource: MockDataSource) {
    private val updateInterval = 2000L

    suspend fun fetchStockPE(symbol: String): Flow<Resource<Company>> {
        return flow {
            emit(Resource.Loading())
            delay(3000)
            while (true) {
                val company = getCompany(symbol)
                if (company != null) {
                    delay(updateInterval)
                    val updatedCompany = company.copy(peRatio = "${company.peRatio.toDouble().plus(1.0)}")
                    mockDataSource.updateCompany(updatedCompany)
                    emit(Resource.Success(updatedCompany))
                } else {
                    emit(Resource.Error(message = "Company not found"))
                }
            }
        }
    }

    suspend fun fetchStockCurrentPrice(symbol: String): Flow<Resource<Stock>> {
        return flow {
            emit(Resource.Loading())
            delay(3000)
            while (true) {
                val stock = getStock(symbol)
                if (stock != null) {
                    delay(updateInterval)
                    val updatedStock = stock.copy(currentPrice = stock.currentPrice + 1)
                    mockDataSource.updateStock(updatedStock)
                    emit(Resource.Success(updatedStock))
                } else {
                    emit(Resource.Error(message = "Stock not found"))
                }
            }
        }
    }

    suspend fun fetchStockHighLow(symbol: String): Flow<Resource<Stock>> {
        return flow {
            emit(Resource.Loading())
            delay(3000)
            while (true) {
                val stock = getStock(symbol)
                if (stock != null) {
                    delay(updateInterval)
                    val updatedStock = stock.copy(high = stock.high + 2, low = stock.low - 1)
                    mockDataSource.updateStock(updatedStock)
                    emit(Resource.Success(updatedStock))
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