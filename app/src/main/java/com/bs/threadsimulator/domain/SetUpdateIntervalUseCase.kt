package com.bs.threadsimulator.domain

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.data.repository.StockRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for setting update intervals and list size for data sources.
 *
 * Allows dynamic configuration of refresh rates for PE, current price, and high/low data streams,
 * as well as the size of the company list to simulate. Executes on the IO dispatcher to ensure
 * non-blocking configuration changes.
 */
class SetUpdateIntervalUseCase
    @Inject
    constructor(
        private val stockRepository: StockRepository,
        private val appDispatchers: AppDispatchers,
    ) {
        /**
         * Sets the update interval for a specific data type.
         *
         * Executes on the IO dispatcher to prevent blocking the main thread.
         *
         * @param intervalType The type of configuration to update
         * @param interval For time-based updates (PE, CURRENT_PRICE, HIGH_LOW): interval in milliseconds.
         *                 For LIST_SIZE: the number of companies to generate
         */
        suspend fun execute(
            intervalType: UpdateIntervalType,
            interval: Long,
        ) {
            withContext(appDispatchers.ioDispatcher) {
                when (intervalType) {
                    UpdateIntervalType.PE -> stockRepository.setUpdateIntervalPE(interval)
                    UpdateIntervalType.CURRENT_PRICE -> stockRepository.setUpdateIntervalCurrentPrice(interval)
                    UpdateIntervalType.HIGH_LOW -> stockRepository.setUpdateIntervalHighLow(interval)
                    UpdateIntervalType.LIST_SIZE -> stockRepository.setListSize(interval)
                }
            }
        }
    }
