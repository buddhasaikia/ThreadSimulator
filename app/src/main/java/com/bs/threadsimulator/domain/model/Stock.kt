package com.bs.threadsimulator.domain.model

import java.math.BigDecimal

/**
 * Domain-level representation of stock price information.
 *
 * This is the domain layer abstraction of stock data. It is semantically distinct from
 * [com.bs.threadsimulator.data.StockInfo] (data layer) to maintain clean separation of concerns.
 *
 * Domain models represent business concepts and rules, independent of how data is persisted
 * or fetched. This model is used by use cases and business logic.
 *
 * @property symbol The stock ticker symbol (e.g., "AAPL")
 * @property openingPrice The price at market open
 * @property closingPrice The price at market close
 * @property low The lowest price during the period
 * @property high The highest price during the period
 * @property currentPrice The current market price
 */
data class Stock(
    val symbol: String = "",
    val openingPrice: BigDecimal = BigDecimal(0.0),
    val closingPrice: BigDecimal = BigDecimal(0.0),
    val low: BigDecimal = BigDecimal(0.0),
    val high: BigDecimal = BigDecimal(0.0),
    val currentPrice: BigDecimal = BigDecimal(0.0),
)
