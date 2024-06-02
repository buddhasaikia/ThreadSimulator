package com.bs.threadsimulator.data

import com.bs.threadsimulator.model.Resource
import com.bs.threadsimulator.model.Stock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DataRepository {
    suspend fun fetchLiveStockStatus(symbol: String): Flow<Resource<Stock>> {
        return flow {
            emit(Resource.Loading())
            while (true) {
                val stock = getStock(symbol)
                if (stock != null) {
                    delay(2000)
                    val updatedStock = stock.copy(currentPrice = stock.currentPrice + 1)
                    val resource = Resource.Success(updatedStock)
                    MockDataSource().updateStock(updatedStock)
                    emit(resource)
                } else {
                    emit(Resource.Error(message = "Stock not found"))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun getStock(symbol: String): Stock? {
        return MockDataSource().getCompanyList().find { it.stock.symbol == symbol }?.stock
    }
}