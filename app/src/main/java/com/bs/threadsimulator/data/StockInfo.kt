package com.bs.threadsimulator.data

import java.math.BigDecimal

data class StockInfo(
    val symbol: String = "",
    val openingPrice: BigDecimal = BigDecimal.ZERO,
    val closingPrice: BigDecimal = BigDecimal.ZERO,
    val low: BigDecimal = BigDecimal.ZERO,
    val high: BigDecimal = BigDecimal.ZERO,
    val currentPrice: BigDecimal = BigDecimal.ZERO,
)
