package com.bs.threadsimulator.data

import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.Resource
import com.bs.threadsimulator.model.Stock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DataRepository @Inject constructor(private val mockDataSource: MockDataSource) {
    suspend fun fetchLiveStockStatus(symbol: String): Flow<Resource<Stock>> {
        return flow {
            emit(Resource.Loading())
            delay(3000)
            while (true) {
                val stock = getStock(symbol)
                if (stock != null) {
                    delay(2000)
                    val updatedStock = stock.copy(currentPrice = stock.currentPrice + 1)
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

    fun getCompanyList(): List<Company> {
        return mockDataSource.getCompanyList()
    }

}