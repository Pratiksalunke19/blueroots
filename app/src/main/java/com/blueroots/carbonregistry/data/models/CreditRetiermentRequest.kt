package com.blueroots.carbonregistry.data.models

data class CreditRetirementRequest(
    val creditIds: List<String> = emptyList(),
    val retirementReason: String = "",
    val beneficiaryName: String = "",
    val retirementNote: String = ""
)

data class CreditRetirementResponse(
    val success: Boolean = false,
    val message: String = "",
    val retirementId: String = "",
    val retiredCredits: List<String> = emptyList(),
    val retirementCertificateUrl: String = "",
    val errors: List<String> = emptyList()
)
