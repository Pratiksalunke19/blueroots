package com.blueroots.carbonregistry.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.data.models.CreditStatus
import com.blueroots.carbonregistry.data.storage.CarbonCreditStorage
import com.blueroots.carbonregistry.data.storage.PortfolioStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CarbonCreditRepository(context: Context) {

    private val storage = CarbonCreditStorage(context)

    // LiveData for credits - we'll manually update this
    private val _allCredits = MutableLiveData<List<CarbonCredit>>()
    val allCredits: LiveData<List<CarbonCredit>> = _allCredits

    init {
        // Load initial data
        _allCredits.value = storage.loadCredits()
    }

    /**
     * Refresh credits from storage
     */
    fun refreshCredits() {
        _allCredits.value = storage.loadCredits()
    }

    /**
     * Add a new credit
     */
    suspend fun addCredit(credit: CarbonCredit) = withContext(Dispatchers.IO) {
        storage.addCredit(credit)
        // Update LiveData on main thread
        withContext(Dispatchers.Main) {
            _allCredits.value = storage.loadCredits()
        }
    }

    /**
     * Update existing credit
     */
    suspend fun updateCredit(credit: CarbonCredit) = withContext(Dispatchers.IO) {
        storage.updateCredit(credit)
        withContext(Dispatchers.Main) {
            _allCredits.value = storage.loadCredits()
        }
    }

    /**
     * Update blockchain info for a credit
     */
    suspend fun updateCreditBlockchainInfo(
        creditId: String,
        transactionHash: String,
        blockchainStatus: String
    ) = withContext(Dispatchers.IO) {
        storage.updateCreditBlockchainInfo(creditId, transactionHash, blockchainStatus)
        withContext(Dispatchers.Main) {
            _allCredits.value = storage.loadCredits()
        }
    }

    /**
     * Get all credits synchronously
     */
    suspend fun getAllCreditsSync(): List<CarbonCredit> = withContext(Dispatchers.IO) {
        storage.loadCredits()
    }

    /**
     * Get credits by project
     */
    suspend fun getCreditsByProject(projectId: String): List<CarbonCredit> = withContext(Dispatchers.IO) {
        storage.getCreditsByProject(projectId)
    }

    /**
     * Get credits by status
     */
    suspend fun getCreditsByStatus(status: CreditStatus): List<CarbonCredit> = withContext(Dispatchers.IO) {
        storage.getCreditsByStatus(status)
    }

    /**
     * Get credits by project name
     */
    suspend fun getCreditsByProjectName(projectName: String): List<CarbonCredit> = withContext(Dispatchers.IO) {
        storage.getCreditsByProjectName(projectName)
    }

    /**
     * Get single credit by ID
     */
    suspend fun getCreditById(id: String): CarbonCredit? = withContext(Dispatchers.IO) {
        storage.getCreditById(id)
    }

    /**
     * Delete credit
     */
    suspend fun deleteCredit(creditId: String) = withContext(Dispatchers.IO) {
        storage.deleteCredit(creditId)
        withContext(Dispatchers.Main) {
            _allCredits.value = storage.loadCredits()
        }
    }

    /**
     * Clear all credits
     */
    suspend fun clearAllCredits() = withContext(Dispatchers.IO) {
        storage.clearAllCredits()
        withContext(Dispatchers.Main) {
            _allCredits.value = emptyList()
        }
    }

    /**
     * Get portfolio statistics
     */
    suspend fun getPortfolioStats(): PortfolioStats = withContext(Dispatchers.IO) {
        storage.getPortfolioStats()
    }

    /**
     * Check if has credits
     */
    suspend fun hasCredits(): Boolean = withContext(Dispatchers.IO) {
        storage.hasCredits()
    }

    /**
     * Get credit count
     */
    suspend fun getCreditCount(): Int = withContext(Dispatchers.IO) {
        storage.getCreditCount()
    }

    /**
     * Generate next batch ID
     */
    suspend fun generateNextBatchId(): String = withContext(Dispatchers.IO) {
        storage.generateNextBatchId()
    }
}
