package com.bs.threadsimulator.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

class MockDataSource @Inject constructor() {
    private suspend fun generateRandomCompany(index: Int): CompanyInfo {
        return withContext(Dispatchers.Default){
            val companyNames = listOf("Apple", "Microsoft", "Amazon", "Alphabet", "Facebook", "Tesla", "NVIDIA", "PayPal", "Intel", "Netflix",
                "Adobe", "Salesforce", "Cisco", "Oracle", "IBM", "Qualcomm", "Shopify", "Square", "Twitter", "Spotify")
            val symbols = listOf("AAPL", "MSFT", "AMZN", "GOOGL", "META", "TSLA", "NVDA", "PYPL", "INTC", "NFLX",
                "ADBE", "CRM", "CSCO", "ORCL", "IBM", "QCOM", "SHOP", "SQ", "TWTR", "SPOT")
            val categoryIndices = (1..4).toList()

            val nameIndex = Random.nextInt(companyNames.size)
            val companyName = companyNames[nameIndex] + " Inc."
            val symbol = symbols[nameIndex] + index.toString().padStart(4, '0')
            val categoryIndex = categoryIndices.random()
            val peRatio = String.format(Locale.getDefault(),"%.2f", Random.nextDouble(10.0, 60.0))
            val previousClosingPrice = Random.nextInt(50, 3500)

            val openingPrice = Random.nextDouble(previousClosingPrice * 0.95, previousClosingPrice * 1.05)
            val closingPrice = Random.nextDouble(openingPrice * 0.95, openingPrice * 1.05)
            val low = String.format(Locale.getDefault(),"%.2f", minOf(openingPrice, closingPrice) * 0.95).toDouble()
            val high = String.format(Locale.getDefault(),"%.2f", maxOf(openingPrice, closingPrice) * 1.05).toDouble()
            val currentPrice = String.format(Locale.getDefault(),"%.2f", Random.nextDouble(low, high)).toDouble()

            CompanyInfo(
                index,
                companyName,
                categoryIndex,
                peRatio,
                previousClosingPrice,
                StockInfo(
                    symbol,
                    BigDecimal(openingPrice).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal(closingPrice).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal(low).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal(high).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal(currentPrice).setScale(2, RoundingMode.HALF_UP)
                )
            )
        }
    }

    suspend fun generateCompanies(n: Int): List<CompanyInfo> {
        return List(n) { generateRandomCompany(it) }
    }

    fun getCompanyList(): List<CompanyInfo> {
        return CompanyList.generatedCompanies
    }
}