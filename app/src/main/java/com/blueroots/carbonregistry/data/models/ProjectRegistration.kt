package com.blueroots.carbonregistry.data.models

data class ProjectRegistration(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val ecosystemType: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProjectResponse(
    val id: String,
    val message: String,
    val success: Boolean
)
