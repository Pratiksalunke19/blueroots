package com.blueroots.carbonregistry.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueroots.carbonregistry.data.models.ProjectRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProjectRegistrationViewModel : ViewModel() {

    private val _submissionResult = MutableLiveData<SubmissionResult>()
    val submissionResult: LiveData<SubmissionResult> = _submissionResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // In-memory storage for demo (replace with actual repository/API calls later)
    private val registeredProjects = mutableListOf<ProjectRegistration>()

    fun submitProject(project: ProjectRegistration) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Simulate API call
                delay(2000)

                // Basic validation
                if (project.projectName.isBlank()) {
                    _submissionResult.value = SubmissionResult.Error("Project name is required")
                    return@launch
                }

                if (project.projectArea <= 0) {
                    _submissionResult.value = SubmissionResult.Error("Project area must be greater than 0")
                    return@launch
                }

                if (project.latitude == 0.0 || project.longitude == 0.0) {
                    _submissionResult.value = SubmissionResult.Error("Valid coordinates are required")
                    return@launch
                }

                // Store project (in real app, this would be an API call)
                registeredProjects.add(project)

                _submissionResult.value = SubmissionResult.Success(
                    "Project '${project.projectName}' has been submitted for review. " +
                            "Project ID: ${project.id.substring(0, 8)}..."
                )

            } catch (e: Exception) {
                _submissionResult.value = SubmissionResult.Error("Failed to submit project: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getRegisteredProjects(): List<ProjectRegistration> {
        return registeredProjects.toList()
    }

    sealed class SubmissionResult {
        data class Success(val message: String) : SubmissionResult()
        data class Error(val message: String) : SubmissionResult()
    }
}
