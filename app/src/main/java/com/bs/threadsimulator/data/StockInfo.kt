package com.bs.threadsimulator.data

import java.math.BigDecimal

data class StockInfo(
    var symbol: String = "",
    var openingPrice: BigDecimal = BigDecimal(0.0),
    var closingPrice: BigDecimal = BigDecimal(0.0),
    var low: BigDecimal = BigDecimal(0.0),
    var high: BigDecimal = BigDecimal(0.0),
    var currentPrice: BigDecimal = BigDecimal(0.0)
)
