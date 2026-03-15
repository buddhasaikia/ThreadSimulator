package com.bs.threadsimulator.domain.model

/**
 * Domain-level representation of company with stock data.
 *
 * This is the domain layer abstraction of company information. It is semantically distinct from
 * [com.bs.threadsimulator.data.CompanyInfo] (data layer) to maintain clean separation of concerns.
 *
 * Domain models represent business concepts independent of data layer implementation details.
 * Use cases operate on CompanyData and business rules, transforming input/output through mappers.
 *
 * @property id Unique identifier for the company
 * @property companyName The name of the company
 * @property categoryIndex Index representing the company's business category
 * @property peRatio The current Price-to-Earnings ratio as a string
 * @property previousClosingPrice The stock price from the previous market close
 * @property stock Detailed stock price information (current, high, low, open, close)
 * @property threadName The name of the thread that last updated this company's data (for metrics)
 */
data class CompanyData(
    val id: Int,
    val companyName: String = "",
    val categoryIndex: Int = 0,
    val peRatio: String = "",
    val previousClosingPrice: Int = 0,
    val stock: Stock = Stock(),
    val threadName: String = "",
)
