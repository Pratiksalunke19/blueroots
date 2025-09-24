package com.blueroots.carbonregistry.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueroots.carbonregistry.data.models.ProjectRegistration
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

    // In-memory storage for demo (replace with actual repository/API calls later)
    private val registeredProjects = mutableListOf<ProjectRegistration>()

    private val hederaService = MockHederaService()

    private val _blockchainRegistration = MutableLiveData<HederaTransactionResult?>()
    val blockchainRegistration: LiveData<HederaTransactionResult?> = _blockchainRegistration

    private val _registrationStatus = MutableLiveData<String>()
    val registrationStatus: LiveData<String> = _registrationStatus

    fun submitProject(projectData: Map<String, Any>) {
        viewModelScope.launch {
            try {
                _registrationStatus.value = "Submitting to Hedera network..."

                // Register project on blockchain
                val blockchainResult = hederaService.registerProject(projectData)

                // Update your existing project submission logic here
                _blockchainRegistration.value = blockchainResult
                _registrationStatus.value = "Project registered on blockchain: ${blockchainResult.transactionId}"

                // Trigger automatic credit issuance after successful registration
                triggerCreditIssuance(blockchainResult.transactionId, projectData)

            } catch (e: Exception) {
                _registrationStatus.value = "Registration failed: ${e.message}"
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
