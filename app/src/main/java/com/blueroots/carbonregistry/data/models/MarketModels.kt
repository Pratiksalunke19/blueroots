package com.blueroots.carbonregistry.data.models

import java.util.Date

data class MarketPricesResponse(
    val success: Boolean = false,
    val timestamp: Date = Date(),
    val prices: List<MarketPrice> = emptyList()
)

data class MarketPrice(
    val creditType: String = "",
    val methodology: String = "",
    val vintage: Int = 2024,
    val currentPrice: Double = 0.0,
    val currency: String = "USD",
    val change24h: Double = 0.0,
    val changePercent: Double = 0.0,
    val volume24h: Double = 0.0,
    val lastUpdated: Date = Date()
)

data class MarketTransaction(
    val id: String = "",
    val transactionType: String = "", // BUY, SELL, TRANSFER, RETIRE
    val creditType: String = "",
    val quantity: Double = 0.0,
    val pricePerTonne: Double = 0.0,
    val totalValue: Double = 0.0,
    val buyerId: String = "",
    val sellerId: String = "",
    val transactionDate: Date = Date(),
    val status: String = ""
)
