package com.blueroots.carbonregistry.data.models

data class CarbonCredit(
    val id: String,
    val projectId: String,
    val amount: Double,
    val status: String,
    val blockchainTxHash: String,
    val issuedDate: Long
)
