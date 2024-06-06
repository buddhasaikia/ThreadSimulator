package com.bs.threadsimulator.data

data class StockInfo(
    var symbol: String = "",
    var openingPrice: Double = 0.0,
    var closingPrice: Double = 0.0,
    var low: Double = 0.0,
    var high: Double = 0.0,
    var currentPrice: Double = 0.0
)
