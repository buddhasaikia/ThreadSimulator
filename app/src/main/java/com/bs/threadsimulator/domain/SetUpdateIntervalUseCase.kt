package com.bs.threadsimulator.domain

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.data.DataRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for setting update intervals for various data sources.
 *
 * Allows dynamic configuration of refresh rates for PE, current price, and high/low data streams.
 * Executes on the IO dispatcher to ensure non-blocking configuration changes.
 */
class SetUpdateIntervalUseCase @Inject constructor(
    private val dataRepository: DataRepository,
    private val appDispatchers: AppDispatchers
) {
    /**
     * Sets the update interval for a specific data type.
     *
     * Executes on the IO dispatcher to prevent blocking the main thread.
     *
     * @param name The type of update interval to configure: "PE", "current_price", "high_low", or "list_size"
     * @param interval The interval in milliseconds between updates
     *
     * @throws IllegalArgumentException if name is not a recognized update type
     */
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