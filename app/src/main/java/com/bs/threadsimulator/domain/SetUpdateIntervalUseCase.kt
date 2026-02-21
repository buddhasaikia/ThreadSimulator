package com.bs.threadsimulator.domain

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.data.DataRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for setting update intervals and list size for data sources.
 *
 * Allows dynamic configuration of refresh rates for PE, current price, and high/low data streams,
 * as well as the size of the company list to simulate. Executes on the IO dispatcher to ensure
 * non-blocking configuration changes.
 */
class SetUpdateIntervalUseCase @Inject constructor(
    private val dataRepository: DataRepository,
    private val appDispatchers: AppDispatchers
) {
    /**
     * Sets the update interval for a specific data type.
     *
     * Executes on the IO dispatcher to prevent blocking the main thread.
     * Unknown update types are silently ignored (no exception thrown).
     *
     * @param name The type of configuration to update: "PE" (PE ratio interval in ms), "current_price" (price update interval in ms),
     *             "high_low" (high/low update interval in ms), or "list_size" (number of companies to simulate)
     * @param interval For time-based updates (PE, current_price, high_low): interval in milliseconds.
     *                 For list_size: the number of companies to generate
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