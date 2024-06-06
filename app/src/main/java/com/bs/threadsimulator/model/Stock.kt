package com.bs.threadsimulator.model

data class Stock(
    val symbol: String,
    val openingPrice: Double,
    val closingPrice: Double,
    var low: Double,
    var high: Double,
    var currentPrice: Double
)
