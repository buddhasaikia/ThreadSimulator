package com.bs.threadsimulator.model

data class Stock(
    val symbol: String,
    val openingPrice: Double,
    val closingPrice: Double,
    val low: Double,
    val high: Double,
    val currentPrice: Double
)
