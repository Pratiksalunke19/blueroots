package com.blueroots.carbonregistry.data.models

data class MonitoringData(
    val projectId: String,
    val dataType: String,
    val timestamp: Long,
    val notes: String,
    val imageUrl: String? = null
)

data class UploadResponse(
    val id: String,
    val message: String,
    val success: Boolean
)
