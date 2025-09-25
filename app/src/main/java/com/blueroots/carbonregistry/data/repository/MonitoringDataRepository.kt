package com.blueroots.carbonregistry.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blueroots.carbonregistry.data.models.*
import com.blueroots.carbonregistry.data.storage.MonitoringDataStorage
import com.blueroots.carbonregistry.data.storage.MonitoringStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MonitoringDataRepository(context: Context) {

    private val storage = MonitoringDataStorage(context)

    // LiveData for monitoring data - we'll manually update this
    private val _allMonitoringData = MutableLiveData<List<MonitoringData>>()
    val allMonitoringData: LiveData<List<MonitoringData>> = _allMonitoringData

    init {
        // Load initial data
        _allMonitoringData.value = storage.loadMonitoringData()
    }

    /**
     * Refresh monitoring data from storage
     */
    fun refreshMonitoringData() {
        _allMonitoringData.value = storage.loadMonitoringData()
    }

    /**
     * Add a new monitoring data entry
     */
    suspend fun addMonitoringData(data: MonitoringData) = withContext(Dispatchers.IO) {
        storage.addMonitoringData(data)
        // Update LiveData on main thread
        withContext(Dispatchers.Main) {
            _allMonitoringData.value = storage.loadMonitoringData()
        }
    }

    /**
     * Update existing monitoring data
     */
    suspend fun updateMonitoringData(data: MonitoringData) = withContext(Dispatchers.IO) {
        storage.updateMonitoringData(data)
        withContext(Dispatchers.Main) {
            _allMonitoringData.value = storage.loadMonitoringData()
        }
    }

    /**
     * Update verification status
     */
    suspend fun updateVerificationStatus(
        dataId: String,
        status: VerificationStatus,
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        storage.updateVerificationStatus(dataId, status, notes)
        withContext(Dispatchers.Main) {
            _allMonitoringData.value = storage.loadMonitoringData()
        }
    }

    /**
     * Update sync status
     */
    suspend fun updateSyncStatus(dataId: String, syncStatus: SyncStatus) = withContext(Dispatchers.IO) {
        storage.updateSyncStatus(dataId, syncStatus)
        withContext(Dispatchers.Main) {
            _allMonitoringData.value = storage.loadMonitoringData()
        }
    }

    /**
     * Get all monitoring data synchronously
     */
    suspend fun getAllMonitoringDataSync(): List<MonitoringData> = withContext(Dispatchers.IO) {
        storage.loadMonitoringData()
    }

    /**
     * Get monitoring data by project
     */
    suspend fun getMonitoringDataByProject(projectId: String): List<MonitoringData> = withContext(Dispatchers.IO) {
        storage.getMonitoringDataByProject(projectId)
    }

    /**
     * Get monitoring data by type
     */
    suspend fun getMonitoringDataByType(dataType: MonitoringDataType): List<MonitoringData> = withContext(Dispatchers.IO) {
        storage.getMonitoringDataByType(dataType)
    }

    /**
     * Get monitoring data by verification status
     */
    suspend fun getMonitoringDataByVerificationStatus(status: VerificationStatus): List<MonitoringData> = withContext(Dispatchers.IO) {
        storage.getMonitoringDataByVerificationStatus(status)
    }

    /**
     * Get monitoring data by sync status
     */
    suspend fun getMonitoringDataBySyncStatus(status: SyncStatus): List<MonitoringData> = withContext(Dispatchers.IO) {
        storage.getMonitoringDataBySyncStatus(status)
    }

    /**
     * Get monitoring data by project name
     */
    suspend fun getMonitoringDataByProjectName(projectName: String): List<MonitoringData> = withContext(Dispatchers.IO) {
        storage.getMonitoringDataByProjectName(projectName)
    }

    /**
     * Get single monitoring data by ID
     */
    suspend fun getMonitoringDataById(id: String): MonitoringData? = withContext(Dispatchers.IO) {
        storage.getMonitoringDataById(id)
    }

    /**
     * Delete monitoring data
     */
    suspend fun deleteMonitoringData(dataId: String) = withContext(Dispatchers.IO) {
        storage.deleteMonitoringData(dataId)
        withContext(Dispatchers.Main) {
            _allMonitoringData.value = storage.loadMonitoringData()
        }
    }

    /**
     * Clear all monitoring data
     */
    suspend fun clearAllMonitoringData() = withContext(Dispatchers.IO) {
        storage.clearAllMonitoringData()
        withContext(Dispatchers.Main) {
            _allMonitoringData.value = emptyList()
        }
    }

    /**
     * Get monitoring statistics
     */
    suspend fun getMonitoringStats(): MonitoringStats = withContext(Dispatchers.IO) {
        storage.getMonitoringStats()
    }

    /**
     * Check if has monitoring data
     */
    suspend fun hasMonitoringData(): Boolean = withContext(Dispatchers.IO) {
        storage.hasMonitoringData()
    }

    /**
     * Get monitoring data count
     */
    suspend fun getMonitoringDataCount(): Int = withContext(Dispatchers.IO) {
        storage.getMonitoringDataCount()
    }

    /**
     * Generate next monitoring data ID
     */
    suspend fun generateNextMonitoringId(): String = withContext(Dispatchers.IO) {
        storage.generateNextMonitoringId()
    }

    /**
     * Get recent monitoring data
     */
    suspend fun getRecentMonitoringData(days: Int = 30): List<MonitoringData> = withContext(Dispatchers.IO) {
        storage.getRecentMonitoringData(days)
    }

    /**
     * Get monitoring data requiring attention
     */
    suspend fun getMonitoringDataRequiringAttention(): List<MonitoringData> = withContext(Dispatchers.IO) {
        storage.getMonitoringDataRequiringAttention()
    }

    /**
     * Bulk sync all local data
     */
    suspend fun syncAllLocalData() = withContext(Dispatchers.IO) {
        val localData = storage.getMonitoringDataBySyncStatus(SyncStatus.LOCAL)
        localData.forEach { data ->
            storage.updateSyncStatus(data.id, SyncStatus.SYNCED)
        }
        withContext(Dispatchers.Main) {
            _allMonitoringData.value = storage.loadMonitoringData()
        }
    }
}
