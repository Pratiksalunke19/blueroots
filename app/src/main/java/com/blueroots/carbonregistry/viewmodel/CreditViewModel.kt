package com.blueroots.carbonregistry.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blueroots.carbonregistry.ai.CarbonOracleService
import com.blueroots.carbonregistry.ai.CarbonPrediction
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.data.models.CreditStatus
import com.blueroots.carbonregistry.data.models.EcosystemType
import com.blueroots.carbonregistry.data.blockchain.MockHederaService
import com.blueroots.carbonregistry.data.blockchain.HederaTransactionResult
import com.blueroots.carbonregistry.data.models.MonitoringData
import com.blueroots.carbonregistry.data.repository.CarbonCreditRepository
import com.blueroots.carbonregistry.data.storage.PortfolioStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class CreditViewModel(application: Application) : AndroidViewModel(application) {

    // Repository with SharedPreferences
    private val repository = CarbonCreditRepository(application.applicationContext)

    // LiveData from Repository
    val creditList: LiveData<List<CarbonCredit>> = repository.allCredits

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Blockchain-related LiveData
    private val _blockchainStatus = MutableLiveData<String>()
    val blockchainStatus: LiveData<String> = _blockchainStatus

    private val _transactionResult = MutableLiveData<HederaTransactionResult?>()
    val transactionResult: LiveData<HederaTransactionResult?> = _transactionResult

    private val _lastIssuedCredit = MutableLiveData<CarbonCredit?>()
    val lastIssuedCredit: LiveData<CarbonCredit?> = _lastIssuedCredit

    // Portfolio stats
    private val _portfolioStats = MutableLiveData<PortfolioStats>()
    val portfolioStats: LiveData<PortfolioStats> = _portfolioStats

    // Hedera service
    private val hederaService = MockHederaService()

    // Filtered credits
    private val _filteredCredits = MutableLiveData<List<CarbonCredit>>()
    val filteredCredits: LiveData<List<CarbonCredit>> = _filteredCredits

    private val carbonOracle = CarbonOracleService()

    private val _prediction = MutableLiveData<CarbonPrediction?>()
    val prediction: LiveData<CarbonPrediction?> get() = _prediction

    private val _isGeneratingPrediction = MutableLiveData<Boolean>()
    val isGeneratingPrediction: LiveData<Boolean> get() = _isGeneratingPrediction


    init {
        loadInitialData()

        // Observe credit list changes and update filtered credits
        creditList.observeForever { credits ->
            _filteredCredits.value = credits
            updatePortfolioStats()
        }
    }

    // NEW: Batch-specific prediction
    private val _batchPrediction = MutableLiveData<Pair<CarbonCredit, CarbonPrediction?>>()
    val batchPrediction: LiveData<Pair<CarbonCredit, CarbonPrediction?>> get() = _batchPrediction

    fun generateBatchSpecificPrediction(credit: CarbonCredit, monitoringData: MonitoringData?) {
        viewModelScope.launch {
            _isGeneratingPrediction.value = true

            try {
                // Call the Oracle with batch-specific data (removed batchContext parameter)
                val prediction = carbonOracle.predictBatchFuture(
                    credit = credit,
                    monitoringData = monitoringData
                )

                _batchPrediction.value = Pair(credit, prediction)

            } catch (e: Exception) {
                Log.e("CreditViewModel", "Batch prediction failed", e)
                _batchPrediction.value = Pair(credit, null)
            } finally {
                _isGeneratingPrediction.value = false
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Check if we have credits, if not, populate with sample data
                val hasCredits = repository.hasCredits()
                if (!hasCredits) {
                    val sampleCredits = generateSampleCredits()
                    sampleCredits.forEach { credit ->
                        repository.addCredit(credit)
                    }
                }

                // Refresh repository to update LiveData
                repository.refreshCredits()

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load carbon credits: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateCarbonPrediction(monitoringData: MonitoringData? = null) {
        viewModelScope.launch {
            _isGeneratingPrediction.value = true

            try {
                // Use real monitoring data if available, otherwise use demo data
                val soilInfo = monitoringData?.soilData?.let { soil ->
                    "Organic Carbon: ${soil.organicCarbonContent}%, pH: ${soil.pH}, " +
                            "Bulk Density: ${soil.bulkDensity}, Salinity: ${soil.salinity}ppt, " +
                            "Moisture: ${soil.moisture}%, Temperature: ${soil.sampleTemperature}°C"
                } ?: "Organic Carbon: 8.7%, pH: 7.8, Bulk Density: 0.85, Salinity: 28ppt, Moisture: 62%, Temperature: 26.5°C"

                val prediction = carbonOracle.predictCarbonCredits(
                    projectName = "Sundarbans Mangrove Restoration Initiative - Phase II",
                    soilData = soilInfo,
                    location = "Sundarbans, West Bengal, India - GPS: 22.1578, 88.9512",
                    currentCredits = 450,
                    projectArea = 1500.0,
                    ecosystemType = "Mangrove wetland restoration"
                )

                _prediction.value = prediction

            } catch (e: Exception) {
                Log.e("CreditViewModel", "Prediction generation failed", e)
                _prediction.value = null
            } finally {
                _isGeneratingPrediction.value = false
            }
        }
    }

    fun refreshCarbonCredits() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                delay(1000) // Simulate network delay
                repository.refreshCredits()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to refresh carbon credits: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterCredits(project: String, status: String) {
        viewModelScope.launch {
            try {
                val allCredits = repository.getAllCreditsSync()
                var filteredList = allCredits

                if (project != "All Projects") {
                    filteredList = filteredList.filter { credit ->
                        credit.projectName.contains(project, ignoreCase = true)
                    }
                }

                if (status != "All Status") {
                    filteredList = filteredList.filter { credit ->
                        credit.status.displayName == status
                    }
                }

                _filteredCredits.value = filteredList
            } catch (e: Exception) {
                _errorMessage.value = "Failed to filter credits: ${e.message}"
            }
        }
    }


    /**
     * Issue new carbon credits via Hedera blockchain and save to SharedPreferences
     */
    fun issueCarbonCreditsFromProject(
        projectId: String,
        projectName: String,
        creditsAmount: Double,
        ecosystemType: EcosystemType,
        location: String
    ) {
        viewModelScope.launch {
            try {
                _blockchainStatus.value = "Processing credit issuance on Hedera network..."
                _isLoading.value = true

                // Generate next batch ID
                val batchId = repository.generateNextBatchId()

                // Simulate blockchain transaction
                val verificationData = mapOf(
                    "projectId" to projectId,
                    "projectName" to projectName,
                    "creditsAmount" to creditsAmount,
                    "ecosystemType" to ecosystemType.name,
                    "location" to location
                )

                val batch = hederaService.issueCarbonCreditBatch(
                    projectId = projectId,
                    creditsAmount = creditsAmount.toInt(),
                    verificationData = verificationData
                )

                // Create new carbon credit with Hedera transaction details
                val newCredit = CarbonCredit(
                    id = UUID.randomUUID().toString(),
                    batchId = batchId,
                    projectId = projectId,
                    projectName = projectName,
                    quantity = creditsAmount,
                    pricePerTonne = Random.nextDouble(75.0, 95.0),
                    totalValue = creditsAmount * Random.nextDouble(75.0, 95.0),
                    status = CreditStatus.ISSUED,
                    issueDate = Date(),
                    vintageYear = Calendar.getInstance().get(Calendar.YEAR),
                    methodology = "Blue Carbon Accelerator v2.1",
                    standard = "VCS + Hedera Consensus",
                    registry = "BlueRoots Hedera Registry",
                    verificationBody = "Hedera Guardian Network",
                    ecosystemType = ecosystemType,
                    location = location,
                    transactionHash = batch.transactionId,
                    verificationHash = batch.verificationHash,
                    blockchainStatus = "VERIFIED_ON_HEDERA",
                    createdAt = Date(),
                    updatedAt = Date()
                )

                // Save to SharedPreferences
                repository.addCredit(newCredit)

                // Update status and result
                _blockchainStatus.value = "✅ Credits successfully issued on Hedera!"
                _transactionResult.value = HederaTransactionResult(
                    transactionId = batch.transactionId,
                    consensusTimestamp = batch.consensusTimestamp,
                    status = "SUCCESS",
                    fee = "0.15 HBAR",
                    memo = "CREDIT_ISSUANCE_${batchId}"
                )

                _lastIssuedCredit.value = newCredit

                println("🔧 DEBUG: Credit saved to SharedPreferences: ${newCredit.batchId}")

            } catch (e: Exception) {
                _blockchainStatus.value = "❌ Credit issuance failed: ${e.message}"
                _errorMessage.value = "Blockchain transaction failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Process project review and issue credits automatically
     */
    fun processProjectReviewAndIssueCredits(
        projectId: String,
        projectName: String,
        area: Double,
        ecosystemType: EcosystemType,
        location: String
    ) {
        viewModelScope.launch {
            try {
                println("🔧 DEBUG: Starting project review for: $projectName")

                _blockchainStatus.value = "Project under review on Hedera Guardian..."
                delay(2000)

                _blockchainStatus.value = "✅ Project approved by verifiers!"
                delay(1000)

                val creditsPerHectare = when (ecosystemType) {
                    EcosystemType.MANGROVE -> Random.nextDouble(8.0, 12.0)
                    EcosystemType.SEAGRASS -> Random.nextDouble(6.0, 10.0)
                    EcosystemType.SALT_MARSH -> Random.nextDouble(7.0, 11.0)
                    EcosystemType.COASTAL_WETLAND -> Random.nextDouble(5.0, 9.0)
                }

                val totalCredits = area * creditsPerHectare

                println("🔧 DEBUG: Calculated credits: $totalCredits for area: $area")

                // Issue credits automatically
                issueCarbonCreditsFromProject(
                    projectId = projectId,
                    projectName = projectName,
                    creditsAmount = totalCredits,
                    ecosystemType = ecosystemType,
                    location = location
                )
            } catch (e: Exception) {
                println("🔧 DEBUG: Error in processProjectReviewAndIssueCredits: ${e.message}")
                _blockchainStatus.value = "❌ Project review failed: ${e.message}"
            }
        }
    }

    /**
     * Get transaction status
     */
    fun checkTransactionStatus(transactionId: String) {
        viewModelScope.launch {
            try {
                val status = hederaService.getTransactionStatus(transactionId)
                _blockchainStatus.value = "Transaction $transactionId status: $status"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to check transaction status: ${e.message}"
            }
        }
    }

    /**
     * Clear blockchain status message
     */
    fun clearBlockchainStatus() {
        _blockchainStatus.value = ""
        _transactionResult.value = null
    }

    /**
     * Update portfolio statistics
     */
    private fun updatePortfolioStats() {
        viewModelScope.launch {
            try {
                val stats = repository.getPortfolioStats()
                _portfolioStats.value = stats
            } catch (e: Exception) {
                // Ignore errors in stats calculation
            }
        }
    }

    /**
     * Delete a credit
     */
    fun deleteCredit(creditId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCredit(creditId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete credit: ${e.message}"
            }
        }
    }

    /**
     * Clear all credits (for testing)
     */
    fun clearAllCredits() {
        viewModelScope.launch {
            try {
                repository.clearAllCredits()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear credits: ${e.message}"
            }
        }
    }

    private fun generateSampleCredits(): List<CarbonCredit> {
        return listOf(
            CarbonCredit(
                id = UUID.randomUUID().toString(),
                batchId = "BCR-2024-001",
                projectId = "proj-001",
                projectName = "Sundarbans Mangrove Restoration",
                quantity = 245.8,
                pricePerTonne = 85.50,
                totalValue = 21017.19,
                status = CreditStatus.VERIFIED,
                issueDate = Calendar.getInstance().apply {
                    set(2024, 8, 15)
                }.time,
                vintageYear = 2024,
                methodology = "VCS VM0007",
                standard = "VCS",
                registry = "Verra Registry",
                verificationBody = "SCS Global Services",
                ecosystemType = EcosystemType.MANGROVE,
                location = "Sundarbans, Bangladesh",
                transactionHash = "0.0.1001@1695123456.123456789",
                verificationHash = "a1b2c3d4e5f6789012345678901234567890123456789012345678901234567890",
                blockchainStatus = "VERIFIED_ON_HEDERA",
                createdAt = Date(),
                updatedAt = Date()
            ),
            CarbonCredit(
                id = UUID.randomUUID().toString(),
                batchId = "BCR-2024-002",
                projectId = "proj-002",
                projectName = "Gulf Coast Blue Carbon Project",
                quantity = 189.3,
                pricePerTonne = 78.25,
                totalValue = 14812.73,
                status = CreditStatus.AVAILABLE,
                issueDate = Calendar.getInstance().apply {
                    set(2024, 7, 28)
                }.time,
                vintageYear = 2024,
                methodology = "Gold Standard Wetland",
                standard = "Gold Standard",
                registry = "Gold Standard Registry",
                verificationBody = "DNV",
                ecosystemType = EcosystemType.SALT_MARSH,
                location = "Louisiana, USA",
                transactionHash = "0.0.1001@1694123456.987654321",
                verificationHash = "b2c3d4e5f6789012345678901234567890123456789012345678901234567890a1",
                blockchainStatus = "VERIFIED_ON_HEDERA",
                createdAt = Date(),
                updatedAt = Date()
            )
            // Add more sample credits as needed...
        )
    }
}
