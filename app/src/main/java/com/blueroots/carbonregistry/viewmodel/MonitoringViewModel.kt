package com.blueroots.carbonregistry.viewmodel

import SingleLiveEvent
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blueroots.carbonregistry.data.models.*
import com.blueroots.carbonregistry.data.repository.MonitoringDataRepository
import com.blueroots.carbonregistry.data.repository.MonitoringRepository
import com.blueroots.carbonregistry.data.storage.MonitoringStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MonitoringViewModel(application: Application) : AndroidViewModel(application) {

    // Repository with SharedPreferences (local storage)
    private val repository = MonitoringDataRepository(application.applicationContext)

    // Repository for Supabase cloud sync
    private val monitoringRepository = MonitoringRepository()

    // LiveData from Repository
    val monitoringDataList: LiveData<List<MonitoringData>> = repository.allMonitoringData

    // Change from regular MutableLiveData to SingleLiveEvent
    private val _uploadResult = SingleLiveEvent<UploadResult>()
    val uploadResult: LiveData<UploadResult> get() = _uploadResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _syncStatus = MutableLiveData<SyncStatus>()
    val syncStatus: LiveData<SyncStatus> = _syncStatus

    // NEW: Cloud sync status message
    private val _cloudSyncStatus = MutableLiveData<String>()
    val cloudSyncStatus: LiveData<String> = _cloudSyncStatus

    // Monitoring statistics
    private val _monitoringStats = MutableLiveData<MonitoringStats>()
    val monitoringStats: LiveData<MonitoringStats> = _monitoringStats

    // Filtered monitoring data
    private val _filteredMonitoringData = MutableLiveData<List<MonitoringData>>()
    val filteredMonitoringData: LiveData<List<MonitoringData>> = _filteredMonitoringData

    init {
        loadInitialData()

        // Observe monitoring data list changes and update filtered data
        monitoringDataList.observeForever { data ->
            _filteredMonitoringData.value = data
            updateMonitoringStats()
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = SyncStatus.SYNCED

            try {
                // Check if we have monitoring data, if not, populate with sample data
                val hasData = repository.hasMonitoringData()
                if (!hasData) {
                    val sampleData = generateSampleMonitoringData()
                    sampleData.forEach { data ->
                        repository.addMonitoringData(data)
                    }
                }

                // Refresh repository to update LiveData
                repository.refreshMonitoringData()

            } catch (e: Exception) {
                _uploadResult.value = UploadResult.Error("Failed to load monitoring data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * NEW: Upload monitoring data with Supabase cloud sync
     * This is the main method to use from Fragment
     */
    fun uploadMonitoringDataWithSync(data: MonitoringData) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _cloudSyncStatus.value = "Uploading monitoring data..."

                // Basic validation
                if (data.dataCollector.isBlank()) {
                    _uploadResult.value = UploadResult.Error("Data collector name is required")
                    return@launch
                }

                if (data.location.latitude == 0.0 || data.location.longitude == 0.0) {
                    _uploadResult.value = UploadResult.Error("Valid GPS coordinates are required")
                    return@launch
                }

                if (data.notes.isBlank()) {
                    _uploadResult.value = UploadResult.Error("Notes are required to describe the monitoring data")
                    return@launch
                }

                // Generate ID if not provided
                val dataId = if (data.id.isBlank()) {
                    repository.generateNextMonitoringId()
                } else {
                    data.id
                }

                // Update data with proper sync and verification status
                val uploadedData = data.copy(
                    id = dataId,
                    syncStatus = SyncStatus.SYNCED,
                    verificationStatus = VerificationStatus.PENDING,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                // 1. Save to local storage first (instant, works offline)
                repository.addMonitoringData(uploadedData)
                Log.d("MonitoringViewModel", "✅ Monitoring data saved locally: $dataId")

                // 2. Sync to Supabase in background
                _cloudSyncStatus.value = "Syncing to cloud..."
                val monitoringId = monitoringRepository.insertMonitoringData(uploadedData)

                if (monitoringId != null) {
                    Log.d("MonitoringViewModel", "✅ Monitoring data synced to Supabase: $monitoringId")
                    _cloudSyncStatus.value = "✅ Synced to cloud"
                    _uploadResult.value = UploadResult.Success(
                        "✅ Monitoring data uploaded successfully!\n" +
                                "Data ID: ${dataId.takeLast(8)}...\n" +
                                "Status: Pending Verification\n" +
                                "🔗 Synced to cloud registry"
                    )
                } else {
                    Log.w("MonitoringViewModel", "⚠️ Monitoring data saved locally but cloud sync failed")
                    _cloudSyncStatus.value = "⚠️ Saved locally (sync pending)"
                    _uploadResult.value = UploadResult.Success(
                        "✅ Monitoring data uploaded locally!\n" +
                                "Data ID: ${dataId.takeLast(8)}...\n" +
                                "Status: Pending Verification\n" +
                                "⚠️ Cloud sync will retry later"
                    )
                }

                println("🔧 DEBUG: Monitoring data saved: $dataId")

            } catch (e: Exception) {
                Log.e("MonitoringViewModel", "❌ Error uploading monitoring data: ${e.message}", e)
                _cloudSyncStatus.value = "❌ Sync error"
                _uploadResult.value = UploadResult.Error("Failed to upload monitoring data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * LEGACY: Old method without cloud sync (kept for backward compatibility)
     * Use uploadMonitoringDataWithSync() instead
     */
    fun uploadMonitoringData(data: MonitoringData) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Simulate API call delay
                delay(2000)

                // Basic validation
                if (data.dataCollector.isBlank()) {
                    _uploadResult.value = UploadResult.Error("Data collector name is required")
                    return@launch
                }

                if (data.location.latitude == 0.0 || data.location.longitude == 0.0) {
                    _uploadResult.value = UploadResult.Error("Valid GPS coordinates are required")
                    return@launch
                }

                if (data.notes.isBlank()) {
                    _uploadResult.value = UploadResult.Error("Notes are required to describe the monitoring data")
                    return@launch
                }

                // Generate ID if not provided
                val dataId = if (data.id.isBlank()) {
                    repository.generateNextMonitoringId()
                } else {
                    data.id
                }

                // Update data with proper sync and verification status
                val uploadedData = data.copy(
                    id = dataId,
                    syncStatus = SyncStatus.SYNCED,
                    verificationStatus = VerificationStatus.PENDING,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                // Save to SharedPreferences
                repository.addMonitoringData(uploadedData)

                _uploadResult.value = UploadResult.Success(
                    "✅ Monitoring data uploaded successfully!\n" +
                            "Data ID: ${dataId.takeLast(8)}...\n" +
                            "Status: Pending Verification\n" +
                            "🔗 Stored in local registry"
                )

                println("🔧 DEBUG: Monitoring data saved to SharedPreferences: $dataId")

            } catch (e: Exception) {
                _uploadResult.value = UploadResult.Error("Failed to upload monitoring data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMonitoringDataByProject(projectId: String) {
        viewModelScope.launch {
            try {
                val data = repository.getMonitoringDataByProject(projectId)
                _filteredMonitoringData.value = data
            } catch (e: Exception) {
                _uploadResult.value = UploadResult.Error("Failed to filter by project: ${e.message}")
            }
        }
    }

    fun getMonitoringDataByType(dataType: MonitoringDataType) {
        viewModelScope.launch {
            try {
                val data = repository.getMonitoringDataByType(dataType)
                _filteredMonitoringData.value = data
            } catch (e: Exception) {
                _uploadResult.value = UploadResult.Error("Failed to filter by type: ${e.message}")
            }
        }
    }

    fun getPendingVerificationData() {
        viewModelScope.launch {
            try {
                val data = repository.getMonitoringDataByVerificationStatus(VerificationStatus.PENDING)
                _filteredMonitoringData.value = data
            } catch (e: Exception) {
                _uploadResult.value = UploadResult.Error("Failed to get pending data: ${e.message}")
            }
        }
    }

    fun syncOfflineData() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.SYNCING

            try {
                // Simulate sync process
                delay(3000)

                // Sync all local data
                repository.syncAllLocalData()

                _syncStatus.value = SyncStatus.SYNCED
                _uploadResult.value = UploadResult.Success("🔄 All monitoring data synchronized successfully!")

            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.ERROR
                _uploadResult.value = UploadResult.Error("Sync failed: ${e.message}")
            }
        }
    }

    /**
     * Update verification status for monitoring data
     */
    fun updateVerificationStatus(dataId: String, status: VerificationStatus, notes: String = "") {
        viewModelScope.launch {
            try {
                repository.updateVerificationStatus(dataId, status, notes)
                _uploadResult.value = UploadResult.Success("Verification status updated to ${status.name}")
            } catch (e: Exception) {
                _uploadResult.value = UploadResult.Error("Failed to update verification: ${e.message}")
            }
        }
    }

    /**
     * Filter monitoring data
     */
    fun filterMonitoringData(projectName: String = "", dataType: MonitoringDataType? = null, verificationStatus: VerificationStatus? = null) {
        viewModelScope.launch {
            try {
                var data = repository.getAllMonitoringDataSync()

                if (projectName.isNotEmpty()) {
                    data = data.filter { it.projectName.contains(projectName, ignoreCase = true) }
                }

                if (dataType != null) {
                    data = data.filter { it.dataType == dataType }
                }

                if (verificationStatus != null) {
                    data = data.filter { it.verificationStatus == verificationStatus }
                }

                _filteredMonitoringData.value = data
            } catch (e: Exception) {
                _uploadResult.value = UploadResult.Error("Failed to filter data: ${e.message}")
            }
        }
    }

    /**
     * Clear all filters and show all data
     */
    fun clearFilters() {
        viewModelScope.launch {
            val allData = repository.getAllMonitoringDataSync()
            _filteredMonitoringData.value = allData
        }
    }

    /**
     * Delete monitoring data
     */
    fun deleteMonitoringData(dataId: String) {
        viewModelScope.launch {
            try {
                repository.deleteMonitoringData(dataId)
                _uploadResult.value = UploadResult.Success("Monitoring data deleted successfully")
            } catch (e: Exception) {
                _uploadResult.value = UploadResult.Error("Failed to delete data: ${e.message}")
            }
        }
    }

    /**
     * Get monitoring data requiring attention
     */
    fun getDataRequiringAttention() {
        viewModelScope.launch {
            try {
                val data = repository.getMonitoringDataRequiringAttention()
                _filteredMonitoringData.value = data
            } catch (e: Exception) {
                _uploadResult.value = UploadResult.Error("Failed to get urgent data: ${e.message}")
            }
        }
    }

    /**
     * Update monitoring statistics
     */
    private fun updateMonitoringStats() {
        viewModelScope.launch {
            try {
                val stats = repository.getMonitoringStats()
                _monitoringStats.value = stats
            } catch (e: Exception) {
                // Ignore errors in stats calculation
            }
        }
    }

    /**
     * Generate sample monitoring data for demo
     */
    private fun generateSampleMonitoringData(): List<MonitoringData> {
        return listOf(
            MonitoringData(
                id = "MON-${System.currentTimeMillis()}-1001",
                projectId = "proj-001",
                projectName = "Sundarbans Mangrove Restoration",
                dataType = MonitoringDataType.SOIL_SAMPLE,
                monitoringDate = Date(),
                reportingPeriod = "Q3 2024",
                location = MonitoringLocation(
                    latitude = 21.9497,
                    longitude = 89.1833,
                    siteDescription = "Mangrove restoration plot A1"
                ),
                soilData = SoilData(
                    sampleId = "SOIL-001",
                    organicCarbonContent = 12.5,
                    pH = 7.2,
                    salinity = 15.0,
                    carbonStock = 85.3
                ),
                dataCollector = "Dr. Ravi Kumar",
                collectorQualifications = "Marine Biology PhD",
                equipmentUsed = listOf("pH meter", "EC meter", "Soil auger"),
                notes = "Soil sample collected from restoration site showing good organic content",
                verificationStatus = VerificationStatus.VERIFIED,
                syncStatus = SyncStatus.SYNCED,
                createdAt = Date(),
                updatedAt = Date()
            ),
            MonitoringData(
                id = "MON-${System.currentTimeMillis()}-1002",
                projectId = "proj-002",
                projectName = "Gulf Coast Blue Carbon Project",
                dataType = MonitoringDataType.VEGETATION_SURVEY,
                monitoringDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -5)
                }.time,
                reportingPeriod = "Q3 2024",
                location = MonitoringLocation(
                    latitude = 29.7604,
                    longitude = -95.3698,
                    siteDescription = "Salt marsh restoration area"
                ),
                vegetationData = VegetationData(
                    plotId = "VEG-001",
                    canopyCover = 75.0,
                    averageHeight = 1.8,
                    stemDensity = 150,
                    healthAssessment = HealthStatus.HEALTHY
                ),
                dataCollector = "Sarah Johnson",
                collectorQualifications = "Wetland Ecologist",
                equipmentUsed = listOf("Quadrat", "Measuring tape", "Camera"),
                notes = "Vegetation showing excellent recovery with 75% canopy cover",
                verificationStatus = VerificationStatus.PENDING,
                syncStatus = SyncStatus.SYNCED,
                priority = Priority.MEDIUM,
                createdAt = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -5)
                }.time,
                updatedAt = Date()
            )
        )
    }

    fun clearUploadResult() {
//        _uploadResult.value = null
    }

    sealed class UploadResult {
        data class Success(val message: String) : UploadResult()
        data class Error(val message: String) : UploadResult()
    }
}
