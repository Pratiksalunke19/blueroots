package com.blueroots.carbonregistry.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.blueroots.carbonregistry.data.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class MonitoringDataStorage(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "monitoring_data_storage",
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    companion object {
        private const val KEY_MONITORING_DATA_LIST = "monitoring_data_list"
        private const val KEY_LAST_MONITORING_ID = "last_monitoring_id"
        private const val KEY_MONITORING_STATS = "monitoring_stats"
    }

    /**
     * Save all monitoring data to SharedPreferences
     */
    fun saveMonitoringData(monitoringDataList: List<MonitoringData>) {
        val dataJson = gson.toJson(monitoringDataList)
        sharedPreferences.edit()
            .putString(KEY_MONITORING_DATA_LIST, dataJson)
            .apply()
    }

    /**
     * Load all monitoring data from SharedPreferences
     */
    fun loadMonitoringData(): List<MonitoringData> {
        val dataJson = sharedPreferences.getString(KEY_MONITORING_DATA_LIST, null)
        return if (dataJson != null) {
            try {
                val type = object : TypeToken<List<MonitoringData>>() {}.type
                gson.fromJson(dataJson, type) ?: emptyList()
            } catch (e: Exception) {
                // If parsing fails, return empty list and clear corrupt data
                clearAllMonitoringData()
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Add a new monitoring data entry
     */
    fun addMonitoringData(data: MonitoringData) {
        val currentData = loadMonitoringData().toMutableList()

        // Check if monitoring data already exists (by ID), if so update it
        val existingIndex = currentData.indexOfFirst { it.id == data.id }
        if (existingIndex != -1) {
            currentData[existingIndex] = data.copy(updatedAt = Date())
        } else {
            // Add new data at the beginning (most recent first)
            currentData.add(0, data)
        }

        saveMonitoringData(currentData)

        // Update last monitoring ID
        sharedPreferences.edit()
            .putString(KEY_LAST_MONITORING_ID, data.id)
            .apply()
    }

    /**
     * Update existing monitoring data
     */
    fun updateMonitoringData(updatedData: MonitoringData) {
        val currentData = loadMonitoringData().toMutableList()
        val index = currentData.indexOfFirst { it.id == updatedData.id }

        if (index != -1) {
            currentData[index] = updatedData.copy(updatedAt = Date())
            saveMonitoringData(currentData)
        }
    }

    /**
     * Update verification status for monitoring data
     */
    fun updateVerificationStatus(dataId: String, status: VerificationStatus, notes: String = "") {
        val currentData = loadMonitoringData().toMutableList()
        val index = currentData.indexOfFirst { it.id == dataId }

        if (index != -1) {
            val updatedData = currentData[index].copy(
                verificationStatus = status,
                notes = if (notes.isNotEmpty()) "${currentData[index].notes}\n\nVerification: $notes" else currentData[index].notes,
                updatedAt = Date()
            )
            currentData[index] = updatedData
            saveMonitoringData(currentData)
        }
    }

    /**
     * Update sync status for monitoring data
     */
    fun updateSyncStatus(dataId: String, syncStatus: SyncStatus) {
        val currentData = loadMonitoringData().toMutableList()
        val index = currentData.indexOfFirst { it.id == dataId }

        if (index != -1) {
            val updatedData = currentData[index].copy(
                syncStatus = syncStatus,
                updatedAt = Date()
            )
            currentData[index] = updatedData
            saveMonitoringData(currentData)
        }
    }

    /**
     * Get monitoring data by project ID
     */
    fun getMonitoringDataByProject(projectId: String): List<MonitoringData> {
        return loadMonitoringData().filter { it.projectId == projectId }
    }

    /**
     * Get monitoring data by data type
     */
    fun getMonitoringDataByType(dataType: MonitoringDataType): List<MonitoringData> {
        return loadMonitoringData().filter { it.dataType == dataType }
    }

    /**
     * Get monitoring data by verification status
     */
    fun getMonitoringDataByVerificationStatus(status: VerificationStatus): List<MonitoringData> {
        return loadMonitoringData().filter { it.verificationStatus == status }
    }

    /**
     * Get monitoring data by sync status
     */
    fun getMonitoringDataBySyncStatus(status: SyncStatus): List<MonitoringData> {
        return loadMonitoringData().filter { it.syncStatus == status }
    }

    /**
     * Get monitoring data by project name (search)
     */
    fun getMonitoringDataByProjectName(projectName: String): List<MonitoringData> {
        return loadMonitoringData().filter {
            it.projectName.contains(projectName, ignoreCase = true)
        }
    }

    /**
     * Get a single monitoring data by ID
     */
    fun getMonitoringDataById(id: String): MonitoringData? {
        return loadMonitoringData().find { it.id == id }
    }

    /**
     * Delete monitoring data
     */
    fun deleteMonitoringData(dataId: String) {
        val currentData = loadMonitoringData().toMutableList()
        currentData.removeAll { it.id == dataId }
        saveMonitoringData(currentData)
    }

    /**
     * Clear all monitoring data
     */
    fun clearAllMonitoringData() {
        sharedPreferences.edit()
            .remove(KEY_MONITORING_DATA_LIST)
            .remove(KEY_LAST_MONITORING_ID)
            .remove(KEY_MONITORING_STATS)
            .apply()
    }

    /**
     * Get monitoring statistics
     */
    fun getMonitoringStats(): MonitoringStats {
        val data = loadMonitoringData()

        val totalEntries = data.size
        val pendingVerification = data.count { it.verificationStatus == VerificationStatus.PENDING }
        val verified = data.count { it.verificationStatus == VerificationStatus.VERIFIED }
        val pendingSync = data.count { it.syncStatus == SyncStatus.LOCAL }
        val projectCount = data.map { it.projectId }.distinct().size

        val dataTypeDistribution = MonitoringDataType.values().associate { type ->
            type.displayName to data.count { it.dataType == type }
        }

        return MonitoringStats(
            totalEntries = totalEntries,
            pendingVerification = pendingVerification,
            verified = verified,
            pendingSync = pendingSync,
            projectCount = projectCount,
            dataTypeDistribution = dataTypeDistribution
        )
    }

    /**
     * Check if storage has any monitoring data
     */
    fun hasMonitoringData(): Boolean {
        return loadMonitoringData().isNotEmpty()
    }

    /**
     * Get total count of monitoring data
     */
    fun getMonitoringDataCount(): Int {
        return loadMonitoringData().size
    }

    /**
     * Generate next monitoring data ID
     */
    fun generateNextMonitoringId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "MON-$timestamp-$random"
    }

    /**
     * Get recent monitoring data (last 30 days)
     */
    fun getRecentMonitoringData(days: Int = 30): List<MonitoringData> {
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }.time

        return loadMonitoringData().filter { it.monitoringDate.after(cutoffDate) }
    }

    /**
     * Get monitoring data requiring attention (urgent priority or overdue)
     */
    fun getMonitoringDataRequiringAttention(): List<MonitoringData> {
        val now = Date()
        return loadMonitoringData().filter { data ->
            data.priority == Priority.URGENT ||
                    (data.submissionDeadline != null && data.submissionDeadline!!.before(now) && !data.isComplete)
        }
    }
}

/**
 * Data class for monitoring statistics
 */
data class MonitoringStats(
    val totalEntries: Int,
    val pendingVerification: Int,
    val verified: Int,
    val pendingSync: Int,
    val projectCount: Int,
    val dataTypeDistribution: Map<String, Int>
)
