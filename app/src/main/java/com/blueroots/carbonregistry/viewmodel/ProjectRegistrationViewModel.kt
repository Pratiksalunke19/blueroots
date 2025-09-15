package com.blueroots.carbonregistry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.blueroots.carbonregistry.data.models.ProjectRegistration
import com.blueroots.carbonregistry.data.models.ProjectResponse
import com.blueroots.carbonregistry.data.api.ApiClient

class ProjectRegistrationViewModel : ViewModel() {
    private val _registrationStatus = MutableLiveData<RegistrationStatus>()
    val registrationStatus: LiveData<RegistrationStatus> = _registrationStatus

    fun registerProject(project: ProjectRegistration) {
        viewModelScope.launch {
            try {
                _registrationStatus.value = RegistrationStatus.Loading
                val response = ApiClient.apiService.registerProject(project)

                if (response.isSuccessful && response.body() != null) {
                    _registrationStatus.value = RegistrationStatus.Success(response.body()!!)
                } else {
                    _registrationStatus.value = RegistrationStatus.Error("Registration failed")
                }
            } catch (e: Exception) {
                _registrationStatus.value = RegistrationStatus.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class RegistrationStatus {
    object Loading : RegistrationStatus()
    data class Success(val response: ProjectResponse) : RegistrationStatus()
    data class Error(val message: String) : RegistrationStatus()
}
