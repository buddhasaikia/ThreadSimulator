package com.bs.threadsimulator.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Company(
    companyName: String = "",
    categoryIndex: Int = 0,
    peRatio: String = "",
    previousClosingPrice: Int = 0,
    stock: Stock = Stock(
        symbol = "",
        openingPrice = 0.0,
        closingPrice = 0.0,
        low = 0.0,
        high = 0.0,
        currentPrice = 0.0
    ),
    threadName: String = ""
) {
    var companyName: String by mutableStateOf(companyName)
    var categoryIndex: Int by mutableIntStateOf(categoryIndex)
    var peRatio: String by mutableStateOf(peRatio)
    var previousClosingPrice: Int by mutableIntStateOf(previousClosingPrice)
    var stock: Stock by mutableStateOf(stock)
    var threadName by mutableStateOf(threadName)
}