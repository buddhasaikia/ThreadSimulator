package com.bs.threadsimulator.common

import com.bs.threadsimulator.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Throttles emissions of a Resource flow to limit update frequency.
 *
 * Prevents excessive updates by only allowing emissions that are at least [windowMs] milliseconds
 * apart. Errors are always emitted immediately, regardless of the throttle window, ensuring
 * that error states are never suppressed.
 *
 * @param T The type of data in the Resource
 * @param windowMs The minimum time in milliseconds between consecutive emissions (default: 16ms)
 * @return A new Flow that emits throttled Resource values
 *
 * @example
 * ```
 * fetchStockData()
 *     .throttleUpdates(100L)  // Emit at most every 100ms
 *     .collect { resource -> updateUI(resource) }
 * ```
 */
fun <T> Flow<Resource<T>>.throttleUpdates(windowMs: Long = 16L): Flow<Resource<T>> =
    flow {
        var lastEmissionTime = 0L
        collect { value ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastEmissionTime >= windowMs || value is Resource.Error) {
                emit(value)
                lastEmissionTime = currentTime
            }
        }
    }
