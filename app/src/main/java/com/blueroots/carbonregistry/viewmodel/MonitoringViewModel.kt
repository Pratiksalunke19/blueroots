package com.blueroots.carbonregistry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.blueroots.carbonregistry.data.models.MonitoringData
import com.blueroots.carbonregistry.data.models.UploadResponse
import com.blueroots.carbonregistry.data.api.ApiClient

class MonitoringViewModel : ViewModel() {
    private val _uploadStatus = MutableLiveData<UploadStatus>()
    val uploadStatus: LiveData<UploadStatus> = _uploadStatus

    fun uploadMonitoringData(data: MonitoringData) {
        viewModelScope.launch {
            try {
                _uploadStatus.value = UploadStatus.Loading
                val response = ApiClient.apiService.uploadMonitoringData(data)

                if (response.isSuccessful && response.body() != null) {
                    _uploadStatus.value = UploadStatus.Success(response.body()!!)
                } else {
                    _uploadStatus.value = UploadStatus.Error("Upload failed")
                }
            } catch (e: Exception) {
                _uploadStatus.value = UploadStatus.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class UploadStatus {
    object Loading : UploadStatus()
    data class Success(val response: UploadResponse) : UploadStatus()
    data class Error(val message: String) : UploadStatus()
}
