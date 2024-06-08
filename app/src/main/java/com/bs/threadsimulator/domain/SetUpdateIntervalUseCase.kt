package com.bs.threadsimulator.domain

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.data.DataRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetUpdateIntervalUseCase @Inject constructor(
    private val dataRepository: DataRepository,
    private val appDispatchers: AppDispatchers
) {
    suspend fun execute(name: String, interval: Long) {
        withContext(appDispatchers.ioDispatcher){
            when (name) {
                "PE" -> dataRepository.setUpdateIntervalPE(interval)
                "current_price" -> dataRepository.setUpdateIntervalCurrentPrice(interval)
                "high_low" -> dataRepository.setUpdateIntervalHighLow(interval)
                "list_size" -> dataRepository.setListSize(interval)
            }
        }
    }
}