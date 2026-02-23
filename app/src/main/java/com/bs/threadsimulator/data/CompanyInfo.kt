package com.bs.threadsimulator.data

data class CompanyInfo(
    val id: Int,
    var companyName: String = "",
    var categoryIndex: Int = 0,
    var peRatio: String = "",
    var previousClosingPrice: Int = 0,
    var stock: StockInfo = StockInfo(),
    var threadName: String = "",
)
