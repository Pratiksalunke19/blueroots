package com.blueroots.carbonregistry.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.data.models.CreditStatus
import com.blueroots.carbonregistry.data.models.EcosystemType
// Add these new imports
import com.blueroots.carbonregistry.data.blockchain.MockHederaService
import com.blueroots.carbonregistry.data.blockchain.HederaTransactionResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

class CreditViewModel : ViewModel() {

    private val _creditList = MutableLiveData<List<CarbonCredit>>()
    val creditList: LiveData<List<CarbonCredit>> = _creditList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Add Hedera-related LiveData
    private val _blockchainStatus = MutableLiveData<String>()
    val blockchainStatus: LiveData<String> = _blockchainStatus

    private val _transactionResult = MutableLiveData<HederaTransactionResult?>()
    val transactionResult: LiveData<HederaTransactionResult?> = _transactionResult

    private val _lastIssuedCredit = MutableLiveData<CarbonCredit?>()
    val lastIssuedCredit: LiveData<CarbonCredit?> = _lastIssuedCredit

    // Add Hedera service
    private val hederaService = MockHederaService()

    private var allCredits = mutableListOf<CarbonCredit>()

    init {
        loadCarbonCredits()
    }

    fun loadCarbonCredits() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Simulate API call
                delay(1000)

                // Generate sample data
                allCredits = generateSampleCredits().toMutableList()
                _creditList.value = allCredits

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load carbon credits: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshCarbonCredits() {
        loadCarbonCredits()
    }

    fun filterCredits(project: String, status: String) {
        var filteredCredits = allCredits.toList() // Convert to List<CarbonCredit>

        if (project != "All Projects") {
            filteredCredits = filteredCredits.filter { it.projectName == project }
        }

        if (status != "All Status") {
            filteredCredits = filteredCredits.filter { it.statusDisplayName == status }
        }

        _creditList.value = filteredCredits // Now this matches List<CarbonCredit>
    }

    /**
     * Issue new carbon credits via Hedera blockchain
     * This is the key method for demo - called when project review is approved
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
                    id = batch.batchId,
                    batchId = batch.batchId,
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
                    // Add blockchain-specific fields
                    transactionHash = batch.transactionId,
                    verificationHash = batch.verificationHash,
                    blockchainStatus = "VERIFIED_ON_HEDERA"
                )

                // Add to the beginning of the list (most recent first)
                allCredits.add(0, newCredit)
                _creditList.value = allCredits.toList()

                // Update status and result
                _blockchainStatus.value = "✅ Credits successfully issued on Hedera!"
                _transactionResult.value = HederaTransactionResult(
                    transactionId = batch.transactionId,
                    consensusTimestamp = batch.consensusTimestamp,
                    status = "SUCCESS",
                    fee = "0.15 HBAR",
                    memo = "CREDIT_ISSUANCE_${batch.batchId}"
                )

                _lastIssuedCredit.value = newCredit

            } catch (e: Exception) {
                _blockchainStatus.value = "❌ Credit issuance failed: ${e.message}"
                _errorMessage.value = "Blockchain transaction failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Simulate project approval and automatic credit issuance
     * Call this when "Submit Project For Review" is clicked
     */
    fun processProjectReviewAndIssueCredits(
        projectId: String,
        projectName: String,
        area: Double, // hectares
        ecosystemType: EcosystemType,
        location: String
    ) {
        viewModelScope.launch {
            // First simulate project review approval
            _blockchainStatus.value = "Project under review on Hedera Guardian..."
            delay(2000)

            _blockchainStatus.value = "✅ Project approved by verifiers!"
            delay(1000)

            // Calculate credits based on area and ecosystem type
            val creditsPerHectare = when (ecosystemType) {
                EcosystemType.MANGROVE -> Random.nextDouble(8.0, 12.0)
                EcosystemType.SEAGRASS -> Random.nextDouble(6.0, 10.0)
                EcosystemType.SALT_MARSH -> Random.nextDouble(7.0, 11.0)
                EcosystemType.COASTAL_WETLAND -> Random.nextDouble(5.0, 9.0)
            }

            val totalCredits = area * creditsPerHectare

            // Issue credits automatically
            issueCarbonCreditsFromProject(
                projectId = projectId,
                projectName = projectName,
                creditsAmount = totalCredits,
                ecosystemType = ecosystemType,
                location = location
            )
        }
    }

    /**
     * Get blockchain transaction status
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

    private fun generateSampleCredits(): List<CarbonCredit> {
        return listOf(
            CarbonCredit(
                id = "1",
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
                // Add blockchain fields to existing sample data
                transactionHash = "0.0.1001@1695123456.123456789",
                verificationHash = "a1b2c3d4e5f6789012345678901234567890123456789012345678901234567890",
                blockchainStatus = "VERIFIED_ON_HEDERA"
            ),
            CarbonCredit(
                id = "2",
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
                blockchainStatus = "VERIFIED_ON_HEDERA"
            ),
            CarbonCredit(
                id = "3",
                batchId = "BCR-2024-003",
                projectId = "proj-003",
                projectName = "Pacific Seagrass Conservation",
                quantity = 156.7,
                pricePerTonne = 92.00,
                totalValue = 14416.40,
                status = CreditStatus.PENDING_VERIFICATION,
                issueDate = Calendar.getInstance().apply {
                    set(2024, 8, 5)
                }.time,
                vintageYear = 2024,
                methodology = "Blue Carbon Accelerator",
                standard = "VCS",
                registry = "Verra Registry",
                verificationBody = "TÜV SÜD",
                ecosystemType = EcosystemType.SEAGRASS,
                location = "California, USA",
                transactionHash = "0.0.1001@1697123456.555666777",
                verificationHash = "c3d4e5f6789012345678901234567890123456789012345678901234567890a1b2",
                blockchainStatus = "PENDING_ON_HEDERA"
            ),
            CarbonCredit(
                id = "4",
                batchId = "BCR-2023-015",
                projectId = "proj-001",
                projectName = "Sundarbans Mangrove Restoration",
                quantity = 98.2,
                pricePerTonne = 75.00,
                totalValue = 7365.00,
                status = CreditStatus.RETIRED,
                issueDate = Calendar.getInstance().apply {
                    set(2023, 11, 12)
                }.time,
                vintageYear = 2023,
                methodology = "VCS VM0007",
                standard = "VCS",
                registry = "Verra Registry",
                verificationBody = "SCS Global Services",
                retirementDate = Calendar.getInstance().apply {
                    set(2024, 2, 15)
                }.time,
                buyer = "Microsoft Corporation",
                retirementReason = "Corporate Net Zero Commitment",
                ecosystemType = EcosystemType.MANGROVE,
                location = "Sundarbans, Bangladesh",
                transactionHash = "0.0.1001@1690123456.111222333",
                verificationHash = "d4e5f6789012345678901234567890123456789012345678901234567890a1b2c3",
                blockchainStatus = "RETIRED_ON_HEDERA"
            ),
            CarbonCredit(
                id = "5",
                batchId = "BCR-2024-004",
                projectId = "proj-004",
                projectName = "Coastal Wetland Recovery",
                quantity = 312.4,
                pricePerTonne = 88.75,
                totalValue = 27725.50,
                status = CreditStatus.ISSUED,
                issueDate = Calendar.getInstance().apply {
                    set(2024, 8, 20)
                }.time,
                vintageYear = 2024,
                methodology = "CDM AMS-III.BF",
                standard = "CDM",
                registry = "CDM Registry",
                verificationBody = "Bureau Veritas",
                ecosystemType = EcosystemType.COASTAL_WETLAND,
                location = "Florida, USA",
                transactionHash = "0.0.1001@1698123456.888999000",
                verificationHash = "e5f6789012345678901234567890123456789012345678901234567890a1b2c3d4",
                blockchainStatus = "VERIFIED_ON_HEDERA"
            )
        )
    }
}
