package com.bs.threadsimulator.data

import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.Stock
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

object CompanyList {
    private val companies = mutableListOf(
        Company(
            "Apple Inc.",
            1,
            "28.5",
            150,
            Stock(
                "AAPL",
                148.0,
                150.0,
                147.0,
                151.0,
                149.5
            )
        ),
        Company(
            "Microsoft Corp.",
            2,
            "35.4",
            260,
            Stock(
                "MSFT",
                258.0,
                260.0,
                257.0,
                261.0,
                259.5
            )
        ),
        Company(
            "Amazon.com Inc.",
            1,
            "60.2",
            3400,
            Stock(
                "AMZN",
                3380.0,
                3400.0,
                3370.0,
                3410.0,
                3395.0
            )
        ),
        Company(
            "Alphabet Inc.",
            3,
            "30.1",
            2400,
            Stock(
                "GOOGL",
                2380.0,
                2400.0,
                2370.0,
                2410.0,
                2395.0
            )
        ),
        Company(
            "Facebook Inc.",
            2,
            "24.8",
            320,
            Stock(
                "META",
                318.0,
                320.0,
                317.0,
                321.0,
                319.5
            )
        ),
        Company(
            "Tesla Inc.",
            4,
            "130.6",
            800,
            Stock(
                "TSLA",
                790.0,
                800.0,
                785.0,
                805.0,
                795.0
            )
        ),
        Company(
            "NVIDIA Corp.",
            1,
            "50.3",
            600,
            Stock(
                "NVDA",
                590.0,
                600.0,
                585.0,
                605.0,
                595.0
            )
        ),
        Company(
            "PayPal Holdings Inc.",
            2,
            "45.1",
            190,
            Stock(
                "PYPL",
                185.0,
                190.0,
                183.0,
                192.0,
                188.5
            )
        ),
        Company(
            "Intel Corp.",
            3,
            "12.5",
            50,
            Stock(
                "INTC",
                49.0,
                50.0,
                48.5,
                51.0,
                49.5
            )
        ),
        Company(
            "Netflix Inc.",
            4,
            "80.4",
            500,
            Stock(
                "NFLX",
                490.0,
                500.0,
                485.0,
                505.0,
                495.0
            )
        ),
        Company(
            "Adobe Inc.",
            1,
            "40.7",
            470,
            Stock(
                "ADBE",
                460.0,
                470.0,
                455.0,
                475.0,
                465.0
            )
        ),
        Company(
            "Salesforce Inc.",
            2,
            "100.1",
            250,
            Stock(
                "CRM",
                245.0,
                250.0,
                243.0,
                252.0,
                247.5
            )
        ),
        Company(
            "Cisco Systems Inc.",
            3,
            "18.3",
            55,
            Stock(
                "CSCO",
                54.0,
                55.0,
                53.5,
                56.0,
                54.5
            )
        ),
        Company(
            "Oracle Corp.",
            4,
            "22.4",
            80,
            Stock(
                "ORCL",
                78.0,
                80.0,
                77.5,
                81.0,
                79.0
            )
        ),
        Company(
            "IBM Corp.",
            1,
            "15.6",
            130,
            Stock(
                "IBM",
                128.0,
                130.0,
                127.0,
                131.0,
                129.5
            )
        ),
        Company(
            "Intel Corp.",
            2,
            "12.5",
            50,
            Stock(
                "INTC",
                49.0,
                50.0,
                48.5,
                51.0,
                49.5
            )
        ),
        Company(
            "Qualcomm Inc.",
            3,
            "23.7",
            150,
            Stock(
                "QCOM",
                148.0,
                150.0,
                147.0,
                151.0,
                149.0
            )
        ),
        Company(
            "Shopify Inc.",
            4,
            "210.0",
            1300,
            Stock(
                "SHOP",
                1280.0,
                1300.0,
                1275.0,
                1310.0,
                1295.0
            )
        ),
        Company(
            "Square Inc.",
            1,
            "150.3",
            270,
            Stock(
                "SQ",
                265.0,
                270.0,
                260.0,
                275.0,
                267.5
            )
        ),
        Company(
            "Twitter Inc.",
            2,
            "80.9",
            70,
            Stock(
                "TWTR",
                68.0,
                70.0,
                67.0,
                72.0,
                69.0
            )
        )
    )
    private val _generatedCompanies: List<CompanyInfo> = MockDataSource().generateCompanies(5)
    val generatedCompanies: List<CompanyInfo>
        get() = _generatedCompanies

}
class MockDataSource @Inject constructor() {
    private fun generateRandomCompany(index: Int): CompanyInfo {
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

        return CompanyInfo(
            companyName,
            categoryIndex,
            peRatio,
            previousClosingPrice,
            StockInfo(
                symbol,
                openingPrice,
                closingPrice,
                low,
                high,
                currentPrice
            )
        )
    }

    fun generateCompanies(n: Int): List<CompanyInfo> {
        return List(n) { generateRandomCompany(it + 1) }
    }

    fun getCompanyList(): List<CompanyInfo> {
        return CompanyList.generatedCompanies
    }
}