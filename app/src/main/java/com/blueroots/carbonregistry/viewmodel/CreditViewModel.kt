package com.blueroots.carbonregistry.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.data.models.CreditStatus
import com.blueroots.carbonregistry.data.models.EcosystemType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class CreditViewModel : ViewModel() {

    private val _creditList = MutableLiveData<List<CarbonCredit>>()
    val creditList: LiveData<List<CarbonCredit>> = _creditList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var allCredits = listOf<CarbonCredit>()

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
                allCredits = generateSampleCredits()
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
        var filteredCredits = allCredits

        if (project != "All Projects") {
            filteredCredits = filteredCredits.filter { it.projectName == project }
        }

        if (status != "All Status") {
            filteredCredits = filteredCredits.filter { it.statusDisplayName == status }
        }

        _creditList.value = filteredCredits
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
                location = "Sundarbans, Bangladesh"
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
                location = "Louisiana, USA"
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
                location = "California, USA"
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
                location = "Sundarbans, Bangladesh"
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
                location = "Florida, USA"
            )
        )
    }
}
