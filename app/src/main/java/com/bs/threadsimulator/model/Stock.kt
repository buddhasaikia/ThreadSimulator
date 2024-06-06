package com.bs.threadsimulator.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.setValue

class Stock(
    val symbol: String = "",
    val openingPrice: Double = 0.0,
    val closingPrice: Double = 0.0,
    low: Double = 0.0,
    high: Double = 0.0,
    currentPrice: Double = 0.0
) {
    var low by mutableDoubleStateOf(low)
    var high by mutableDoubleStateOf(high)
    var currentPrice by mutableDoubleStateOf(currentPrice)
}
