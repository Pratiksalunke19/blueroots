package com.blueroots.carbonregistry.data.models

import java.util.Date

data class UploadResponse(
    val success: Boolean = false,
    val message: String = "",
    val dataId: String = "",
    val uploadId: String = "",
    val verificationStatus: String = "",
    val qcChecks: List<QualityCheckResult> = emptyList(),
    val timestamp: Date = Date(),
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

data class QualityCheckResult(
    val checkName: String = "",
    val status: String = "", // PASS, FAIL, WARNING
    val message: String = "",
    val details: String = ""
)
