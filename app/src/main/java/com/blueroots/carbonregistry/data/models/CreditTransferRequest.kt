package com.blueroots.carbonregistry.data.models

data class CreditTransferRequest(
    val creditIds: List<String> = emptyList(),
    val recipientId: String = "",
    val transferReason: String = "",
    val notes: String = ""
)

data class CreditTransferResponse(
    val success: Boolean = false,
    val message: String = "",
    val transferId: String = "",
    val transferredCredits: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)
