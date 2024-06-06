package com.bs.threadsimulator.domain

import com.bs.threadsimulator.data.DataRepository
import javax.inject.Inject

class SetUpdateIntervalUseCase @Inject constructor(private val dataRepository: DataRepository) {
    fun execute(name: String, interval: Long) {
        when(name) {
            "PE" -> dataRepository.setUpdateIntervalPE(interval)
            "current_price" -> dataRepository.setUpdateIntervalCurrentPrice(interval)
            "high_low" -> dataRepository.setUpdateIntervalHighLow(interval)
            "list_size" -> dataRepository.setListSize(interval)
        }
    }
}