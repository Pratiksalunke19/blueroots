package com.blueroots.carbonregistry.data.models

import java.util.Date

data class VerificationRequest(
    val projectId: String = "",
    val dataIds: List<String> = emptyList(),
    val verificationType: String = "",
    val requestedVerificationBody: String = "",
    val priority: String = "NORMAL",
    val notes: String = ""
)

data class VerificationResponse(
    val success: Boolean = false,
    val message: String = "",
    val requestId: String = "",
    val estimatedCompletionDate: Date? = null,
    val cost: Double = 0.0,
    val errors: List<String> = emptyList()
)

// RENAMED: VerificationStatus -> VerificationRequestStatus to avoid conflict
data class VerificationRequestStatus(
    val requestId: String = "",
    val status: String = "",
    val progress: Int = 0,
    val verificationBody: String = "",
    val assignedVerifier: String = "",
    val startDate: Date? = null,
    val expectedCompletionDate: Date? = null,
    val actualCompletionDate: Date? = null,
    val findings: List<VerificationFinding> = emptyList(),
    val documents: List<String> = emptyList(),
    val cost: Double = 0.0
)

data class VerificationFinding(
    val type: String = "", // CONFORMITY, NON_CONFORMITY, OBSERVATION
    val severity: String = "", // MAJOR, MINOR, INFO
    val description: String = "",
    val recommendation: String = "",
    val status: String = "" // OPEN, CLOSED, PENDING
)
