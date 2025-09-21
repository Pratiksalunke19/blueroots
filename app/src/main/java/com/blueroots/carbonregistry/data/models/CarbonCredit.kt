package com.blueroots.carbonregistry.data.models

import java.util.Date

data class CarbonCredit(
    val id: String = "",
    val batchId: String = "",
    val projectId: String = "",
    val projectName: String = "",
    val quantity: Double = 0.0, // tCO2e
    val pricePerTonne: Double = 0.0, // USD per tCO2e
    val totalValue: Double = 0.0, // USD
    val status: CreditStatus = CreditStatus.PENDING_VERIFICATION,
    val issueDate: Date = Date(),
    val vintageYear: Int = 2024,
    val expiryDate: Date? = null,
    val methodology: String = "",
    val standard: String = "", // VCS, Gold Standard, etc.
    val registry: String = "",
    val serialNumbers: List<String> = emptyList(),
    val verificationBody: String = "",
    val verificationDate: Date? = null,
    val retirementDate: Date? = null,
    val buyer: String? = null,
    val retirementReason: String? = null,
    val additionalCertifications: List<String> = emptyList(),
    val cobenefits: List<String> = emptyList(),
    val location: String = "",
    val ecosystemType: EcosystemType = EcosystemType.MANGROVE,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    // Computed property for status display
    val statusDisplayName: String
        get() = when (status) {
            CreditStatus.PENDING_VERIFICATION -> "Pending Verification"
            CreditStatus.VERIFIED -> "Verified"
            CreditStatus.ISSUED -> "Issued"
            CreditStatus.AVAILABLE -> "Available"
            CreditStatus.RESERVED -> "Reserved"
            CreditStatus.TRANSFERRED -> "Transferred"
            CreditStatus.RETIRED -> "Retired"
            CreditStatus.CANCELLED -> "Cancelled"
        }
}

enum class CreditStatus {
    PENDING_VERIFICATION,
    VERIFIED,
    ISSUED,
    AVAILABLE,
    RESERVED,
    TRANSFERRED,
    RETIRED,
    CANCELLED
}
