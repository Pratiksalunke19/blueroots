package com.blueroots.carbonregistry.data.models

import java.util.Date

data class FileUploadResponse(
    val success: Boolean = false,
    val message: String = "",
    val fileId: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val fileSize: Long = 0L,
    val mimeType: String = "",
    val errors: List<String> = emptyList()
)

data class FileInfo(
    val id: String = "",
    val fileName: String = "",
    val originalFileName: String = "",
    val fileUrl: String = "",
    val fileSize: Long = 0L,
    val mimeType: String = "",
    val uploadDate: Date = Date(),
    val uploadedBy: String = "",
    val projectId: String = "",
    val fileType: String = ""
)

data class DeleteResponse(
    val success: Boolean = false,
    val message: String = ""
)
