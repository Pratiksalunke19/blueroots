package com.blueroots.carbonregistry.data.models

import java.util.Date

data class ProjectResponse(
    val success: Boolean = false,
    val message: String = "",
    val projectId: String = "",
    val project: ProjectRegistration? = null,
    val registrationNumber: String = "",
    val status: String = "",
    val timestamp: Date = Date(),
    val errors: List<String> = emptyList()
)
