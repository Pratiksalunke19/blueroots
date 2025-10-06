package com.blueroots.carbonregistry.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueroots.carbonregistry.data.models.ProjectRegistration
import com.blueroots.carbonregistry.data.repository.ProjectRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.blueroots.carbonregistry.data.blockchain.MockHederaService
import com.blueroots.carbonregistry.data.blockchain.HederaTransactionResult
import kotlin.random.Random

class ProjectRegistrationViewModel : ViewModel() {

    private val _submissionResult = MutableLiveData<SubmissionResult>()
    val submissionResult: LiveData<SubmissionResult> = _submissionResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    // In-memory storage for demo (replace with actual repository/API calls later)
    private val registeredProjects = mutableListOf<ProjectRegistration>()

    private val hederaService = MockHederaService()
    private val projectRepository = ProjectRepository()

    private val _blockchainRegistration = MutableLiveData<HederaTransactionResult?>()
    val blockchainRegistration: LiveData<HederaTransactionResult?> = _blockchainRegistration

    private val _registrationStatus = MutableLiveData<String>()
    val registrationStatus: LiveData<String> = _registrationStatus

    /**
     * Save project both locally AND sync to Supabase
     * This is the main method to use from the Fragment
     */
    fun saveProjectWithSync(project: ProjectRegistration) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _syncStatus.value = "Saving project..."

                // 1. Save locally first (instant, works offline)
                saveProjectLocally(project)
                Log.d("ProjectViewModel", "✅ Project saved locally: ${project.projectName}")

                // 2. Sync to Supabase in background
                _syncStatus.value = "Syncing to cloud..."
                val projectId = projectRepository.insertProject(project)

                if (projectId != null) {
                    Log.d("ProjectViewModel", "✅ Project synced to Supabase: $projectId")
                    _syncStatus.value = "✅ Synced to cloud"
                    _submissionResult.value = SubmissionResult.Success(
                        "Project registered successfully and synced to cloud!"
                    )
                } else {
                    Log.w("ProjectViewModel", "⚠️ Project saved locally but cloud sync failed")
                    _syncStatus.value = "⚠️ Saved locally (sync pending)"
                    _submissionResult.value = SubmissionResult.Success(
                        "Project registered locally. Cloud sync will retry later."
                    )
                }

            } catch (e: Exception) {
                Log.e("ProjectViewModel", "❌ Error saving project: ${e.message}", e)
                _syncStatus.value = "❌ Sync error"
                _submissionResult.value = SubmissionResult.Error(
                    "Failed to save project: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Save project locally (can be extended with local database if needed)
     */
    private fun saveProjectLocally(project: ProjectRegistration) {
        registeredProjects.add(project)
        // TODO: If you have Room database, save to local DB here
    }

    /**
     * Legacy method - kept for backward compatibility
     * Prefer using saveProjectWithSync() instead
     */
    fun submitProject(projectData: Map<String, Any>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _registrationStatus.value = "Submitting to Hedera network..."

                // Register project on blockchain
                val blockchainResult = hederaService.registerProject(projectData)

                // Update blockchain registration
                _blockchainRegistration.value = blockchainResult
                _registrationStatus.value = "Project registered on blockchain: ${blockchainResult.transactionId}"

                // Trigger automatic credit issuance after successful registration
                triggerCreditIssuance(blockchainResult.transactionId, projectData)

                _submissionResult.value = SubmissionResult.Success(
                    "Project registered on Hedera blockchain!"
                )

            } catch (e: Exception) {
                _registrationStatus.value = "Registration failed: ${e.message}"
                _submissionResult.value = SubmissionResult.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun triggerCreditIssuance(transactionId: String, projectData: Map<String, Any>) {
        // Simulate automatic credit calculation based on project data
        val estimatedCredits = calculateEstimatedCredits(projectData)

        // Issue initial credit batch
        val creditBatch = hederaService.issueCarbonCreditBatch(
            projectId = transactionId.substringAfter("@").take(8),
            creditsAmount = estimatedCredits,
            verificationData = projectData
        )

        _registrationStatus.value = "Credits issued: ${creditBatch.batchId} (${creditBatch.credits} credits)"
    }

    private fun calculateEstimatedCredits(projectData: Map<String, Any>): Int {
        // Mock calculation - in real app this would be complex
        return Random.nextInt(50, 500)
    }

    fun getRegisteredProjects(): List<ProjectRegistration> {
        return registeredProjects.toList()
    }

    sealed class SubmissionResult {
        data class Success(val message: String) : SubmissionResult()
        data class Error(val message: String) : SubmissionResult()
    }
}
