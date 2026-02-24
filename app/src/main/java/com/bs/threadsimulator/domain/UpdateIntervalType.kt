package com.bs.threadsimulator.domain

/**
 * Enum representing the types of updates that can be configured in the stock data repository.
 *
 * This provides type-safe routing instead of error-prone string-based parameters,
 * eliminating runtime risks from typos or invalid values.
 */
enum class UpdateIntervalType {
    /**
     * Update interval for PE ratio data fetches (in milliseconds)
     */
    PE,

    /**
     * Update interval for current price data fetches (in milliseconds)
     */
    CURRENT_PRICE,

    /**
     * Update interval for high/low price data fetches (in milliseconds)
     */
    HIGH_LOW,

    /**
     * List size - the number of companies to include in the simulation (not a time interval)
     */
    LIST_SIZE,
}
