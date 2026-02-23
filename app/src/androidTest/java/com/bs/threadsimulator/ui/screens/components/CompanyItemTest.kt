package com.bs.threadsimulator.ui.screens.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.Stock
import com.bs.threadsimulator.ui.theme.ThreadSimulatorTheme
import java.math.BigDecimal
import org.junit.Rule
import org.junit.Test

class CompanyItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCompany = Company(
        companyName = "Apple Inc.",
        stock = Stock(
            symbol = "AAPL",
            currentPrice = BigDecimal("150.50"),
            high = BigDecimal("155.25"),
            low = BigDecimal("145.75"),
            openingPrice = BigDecimal("148.00")
        ),
        peRatio = "25.50",
        previousClosingPrice = 149
    )

    @Test
    fun testCompanyItemDisplaysSymbol() {
        composeTestRule.setContent {
            ThreadSimulatorTheme {
                CompanyItem(company = testCompany)
            }
        }

        composeTestRule.onNodeWithText("AAPL").assertIsDisplayed()
    }

    @Test
    fun testCompanyItemDisplaysCompanyName() {
        composeTestRule.setContent {
            ThreadSimulatorTheme {
                CompanyItem(company = testCompany)
            }
        }

        composeTestRule.onNodeWithText("(Apple Inc.)").assertIsDisplayed()
    }

    @Test
    fun testCompanyItemDisplaysPERatio() {
        composeTestRule.setContent {
            ThreadSimulatorTheme {
                CompanyItem(company = testCompany)
            }
        }

        composeTestRule.onNodeWithText("PE: 25.50").assertIsDisplayed()
    }

    @Test
    fun testCompanyItemDisplaysClosingPrice() {
        composeTestRule.setContent {
            ThreadSimulatorTheme {
                CompanyItem(company = testCompany)
            }
        }

        composeTestRule.onNodeWithText("Closing Price: 149").assertIsDisplayed()
    }

    @Test
    fun testCompanyItemDisplaysCurrentPrice() {
        composeTestRule.setContent {
            ThreadSimulatorTheme {
                CompanyItem(company = testCompany)
            }
        }

        composeTestRule.onNodeWithText("Price: 150.50").assertIsDisplayed()
    }

    @Test
    fun testCompanyItemDisplaysHighPrice() {
        composeTestRule.setContent {
            ThreadSimulatorTheme {
                CompanyItem(company = testCompany)
            }
        }

        composeTestRule.onNodeWithText("High: 155.25").assertIsDisplayed()
    }

    @Test
    fun testCompanyItemDisplaysLowPrice() {
        composeTestRule.setContent {
            ThreadSimulatorTheme {
                CompanyItem(company = testCompany)
            }
        }

        composeTestRule.onNodeWithText("Low: 145.75").assertIsDisplayed()
    }

    @Test
    fun testCompanyItemDisplaysThreadName() {
        val companyWithThread = testCompany.apply { threadName = "Worker-Thread-1" }

        composeTestRule.setContent {
            ThreadSimulatorTheme {
                CompanyItem(company = companyWithThread)
            }
        }

        composeTestRule.onNodeWithText("Worker-Thread-1").assertIsDisplayed()
    }

    @Test
    fun testCompanyItemCardIsClickable() {
        composeTestRule.setContent {
            ThreadSimulatorTheme {
                CompanyItem(company = testCompany)
            }
        }

        composeTestRule.onNode(hasAnyDescendant(hasText("AAPL")))
            .assertExists()
    }
}
