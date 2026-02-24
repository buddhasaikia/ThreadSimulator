package com.bs.threadsimulator.data

import java.math.BigDecimal

data class StockInfo(
    var symbol: String = "",
    var openingPrice: BigDecimal = BigDecimal.ZERO,
    var closingPrice: BigDecimal = BigDecimal.ZERO,
    var low: BigDecimal = BigDecimal.ZERO,
    var high: BigDecimal = BigDecimal.ZERO,
    var currentPrice: BigDecimal = BigDecimal.ZERO,
)
