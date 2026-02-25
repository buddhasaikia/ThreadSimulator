package com.bs.threadsimulator.data

data class CompanyInfo(
    val id: Int,
    val companyName: String = "",
    val categoryIndex: Int = 0,
    val peRatio: String = "",
    val previousClosingPrice: Int = 0,
    val stock: StockInfo = StockInfo(),
    val threadName: String = "",
)
