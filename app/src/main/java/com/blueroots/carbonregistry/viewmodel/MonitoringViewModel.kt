package com.blueroots.carbonregistry.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueroots.carbonregistry.data.models.MonitoringData
import com.blueroots.carbonregistry.data.models.SyncStatus
import com.blueroots.carbonregistry.data.models.VerificationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MonitoringViewModel : ViewModel() {

    private val _uploadResult = MutableLiveData<UploadResult>()
    val uploadResult: LiveData<UploadResult> = _uploadResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _monitoringDataList = MutableLiveData<List<MonitoringData>>()
    val monitoringDataList: LiveData<List<MonitoringData>> = _monitoringDataList

    private val _syncStatus = MutableLiveData<SyncStatus>()
    val syncStatus: LiveData<SyncStatus> = _syncStatus

    // In-memory storage for demo (replace with actual repository/API calls later)
    private val monitoringData = mutableListOf<MonitoringData>()

    init {
        _monitoringDataList.value = monitoringData
        _syncStatus.value = SyncStatus.SYNCED
    }

    fun uploadMonitoringData(data: MonitoringData) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Simulate API call
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

                // Update sync status
                val uploadedData = data.copy(
                    syncStatus = SyncStatus.SYNCED,
                    verificationStatus = VerificationStatus.PENDING
                )

                // Store data (in real app, this would be an API call)
                monitoringData.add(uploadedData)
                _monitoringDataList.value = monitoringData.toList()

                _uploadResult.value = UploadResult.Success(
                    "Monitoring data uploaded successfully. " +
                            "Data ID: ${data.id.substring(0, 8)}... " +
                            "Status: Pending Verification"
                )

            } catch (e: Exception) {
                _uploadResult.value = UploadResult.Error("Failed to upload monitoring data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMonitoringDataByProject(projectId: String): List<MonitoringData> {
        return monitoringData.filter { it.projectId == projectId }
    }

    fun getMonitoringDataByType(dataType: com.blueroots.carbonregistry.data.models.MonitoringDataType): List<MonitoringData> {
        return monitoringData.filter { it.dataType == dataType }
    }

    fun getPendingVerificationData(): List<MonitoringData> {
        return monitoringData.filter { it.verificationStatus == VerificationStatus.PENDING }
    }

    fun syncOfflineData() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.SYNCING

            try {
                // Simulate sync process
                delay(3000)

                // Update all local data to synced
                val syncedData = monitoringData.map {
                    if (it.syncStatus == SyncStatus.LOCAL) {
                        it.copy(syncStatus = SyncStatus.SYNCED)
                    } else it
                }

                monitoringData.clear()
                monitoringData.addAll(syncedData)
                _monitoringDataList.value = monitoringData.toList()
                _syncStatus.value = SyncStatus.SYNCED

            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.ERROR
            }
        }
    }

    sealed class UploadResult {
        data class Success(val message: String) : UploadResult()
        data class Error(val message: String) : UploadResult()
    }
}
