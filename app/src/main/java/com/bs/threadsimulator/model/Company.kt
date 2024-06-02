package com.bs.threadsimulator.model

data class Company(
    val companyName: String,
    val categoryIndex: Int,
    val peRatio: String,
    val previousClosingPrice: Int,
    var stock: Stock
)