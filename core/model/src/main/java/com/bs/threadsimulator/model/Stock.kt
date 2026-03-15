package com.bs.threadsimulator.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.math.BigDecimal

class Stock(
    val symbol: String = "",
    val openingPrice: BigDecimal = BigDecimal(0.0),
    val closingPrice: BigDecimal = BigDecimal(0.0),
    low: BigDecimal = BigDecimal(0.0),
    high: BigDecimal = BigDecimal(0.0),
    currentPrice: BigDecimal = BigDecimal(0.0),
) {
    var low by mutableStateOf(low)
    var high by mutableStateOf(high)
    var currentPrice by mutableStateOf(currentPrice)
}
