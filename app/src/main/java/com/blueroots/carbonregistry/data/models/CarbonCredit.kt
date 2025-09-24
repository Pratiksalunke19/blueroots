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
    val updatedAt: Date = Date(),

    // Blockchain-specific fields for Hedera integration
    val transactionHash: String? = null,
    val verificationHash: String? = null,
    val blockchainStatus: String = "VERIFIED_ON_HEDERA"
) {
    // Computed property for status display (keeping for backward compatibility)
    val statusDisplayName: String
        get() = status.displayName
}

enum class CreditStatus(val displayName: String) {
    PENDING_VERIFICATION("Pending Verification"),
    VERIFIED("Verified"),
    ISSUED("Issued"),
    AVAILABLE("Available"),
    RESERVED("Reserved"),
    TRANSFERRED("Transferred"),
    RETIRED("Retired"),
    CANCELLED("Cancelled")
}

