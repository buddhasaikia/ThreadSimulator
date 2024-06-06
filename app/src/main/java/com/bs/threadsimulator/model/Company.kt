package com.bs.threadsimulator.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bs.threadsimulator.data.CompanyInfo

class Company(
    companyName: String = "",
    categoryIndex: Int = 0,
    peRatio: String = "",
    previousClosingPrice: Int = 0,
    stock: Stock = Stock(),
    threadName: String = ""
) {
    var companyName: String by mutableStateOf(companyName)
    var categoryIndex: Int by mutableIntStateOf(categoryIndex)
    var peRatio: String by mutableStateOf(peRatio)
    var previousClosingPrice: Int by mutableIntStateOf(previousClosingPrice)
    var stock: Stock by mutableStateOf(stock)
    var threadName by mutableStateOf(threadName)
}

fun CompanyInfo.toCompany(): Company {
    return Company(
        companyName = companyName,
        categoryIndex = categoryIndex,
        peRatio = peRatio,
        previousClosingPrice = previousClosingPrice,
        threadName = threadName,
        stock = Stock(
            stock.symbol,
            stock.openingPrice,
            stock.closingPrice,
            stock.low,
            stock.high
        )
    )
}