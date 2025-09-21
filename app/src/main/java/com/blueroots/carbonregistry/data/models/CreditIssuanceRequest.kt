package com.blueroots.carbonregistry.data.models

data class CreditIssuanceRequest(
    val projectId: String = "",
    val monitoringDataIds: List<String> = emptyList(),
    val quantity: Double = 0.0,
    val vintageYear: Int = 2024,
    val methodology: String = "",
    val verificationBodyId: String = "",
    val notes: String = ""
)

data class CreditIssuanceResponse(
    val success: Boolean = false,
    val message: String = "",
    val batchId: String = "",
    val creditIds: List<String> = emptyList(),
    val quantity: Double = 0.0,
    val errors: List<String> = emptyList()
)
