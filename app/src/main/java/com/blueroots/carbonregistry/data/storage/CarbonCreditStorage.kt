package com.blueroots.carbonregistry.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.data.models.CreditStatus
import com.blueroots.carbonregistry.data.models.EcosystemType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class CarbonCreditStorage(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "carbon_credits_storage",
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    companion object {
        private const val KEY_CARBON_CREDITS = "carbon_credits_list"
        private const val KEY_LAST_BATCH_ID = "last_batch_id"
        private const val KEY_PORTFOLIO_STATS = "portfolio_stats"
    }

    /**
     * Save all carbon credits to SharedPreferences
     */
    fun saveCredits(credits: List<CarbonCredit>) {
        val creditsJson = gson.toJson(credits)
        sharedPreferences.edit()
            .putString(KEY_CARBON_CREDITS, creditsJson)
            .apply()
    }

    /**
     * Load all carbon credits from SharedPreferences
     */
    fun loadCredits(): List<CarbonCredit> {
        val creditsJson = sharedPreferences.getString(KEY_CARBON_CREDITS, null)
        return if (creditsJson != null) {
            try {
                val type = object : TypeToken<List<CarbonCredit>>() {}.type
                gson.fromJson(creditsJson, type) ?: emptyList()
            } catch (e: Exception) {
                // If parsing fails, return empty list and clear corrupt data
                clearAllCredits()
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Add a new carbon credit
     */
    fun addCredit(credit: CarbonCredit) {
        val currentCredits = loadCredits().toMutableList()

        // Check if credit already exists (by ID), if so update it
        val existingIndex = currentCredits.indexOfFirst { it.id == credit.id }
        if (existingIndex != -1) {
            currentCredits[existingIndex] = credit
        } else {
            // Add new credit at the beginning (most recent first)
            currentCredits.add(0, credit)
        }

        saveCredits(currentCredits)

        // Update last batch ID
        sharedPreferences.edit()
            .putString(KEY_LAST_BATCH_ID, credit.batchId)
            .apply()
    }

    /**
     * Update an existing credit
     */
    fun updateCredit(updatedCredit: CarbonCredit) {
        val currentCredits = loadCredits().toMutableList()
        val index = currentCredits.indexOfFirst { it.id == updatedCredit.id }

        if (index != -1) {
            currentCredits[index] = updatedCredit.copy(updatedAt = Date())
            saveCredits(currentCredits)
        }
    }

    /**
     * Update blockchain information for a credit
     */
    fun updateCreditBlockchainInfo(creditId: String, transactionHash: String, blockchainStatus: String) {
        val currentCredits = loadCredits().toMutableList()
        val index = currentCredits.indexOfFirst { it.id == creditId }

        if (index != -1) {
            val updatedCredit = currentCredits[index].copy(
                transactionHash = transactionHash,
                blockchainStatus = blockchainStatus,
                updatedAt = Date()
            )
            currentCredits[index] = updatedCredit
            saveCredits(currentCredits)
        }
    }

    /**
     * Get credits by project ID
     */
    fun getCreditsByProject(projectId: String): List<CarbonCredit> {
        return loadCredits().filter { it.projectId == projectId }
    }

    /**
     * Get credits by status
     */
    fun getCreditsByStatus(status: CreditStatus): List<CarbonCredit> {
        return loadCredits().filter { it.status == status }
    }

    /**
     * Get credits by project name (search)
     */
    fun getCreditsByProjectName(projectName: String): List<CarbonCredit> {
        return loadCredits().filter {
            it.projectName.contains(projectName, ignoreCase = true)
        }
    }

    /**
     * Get a single credit by ID
     */
    fun getCreditById(id: String): CarbonCredit? {
        return loadCredits().find { it.id == id }
    }

    /**
     * Delete a credit
     */
    fun deleteCredit(creditId: String) {
        val currentCredits = loadCredits().toMutableList()
        currentCredits.removeAll { it.id == creditId }
        saveCredits(currentCredits)
    }

    /**
     * Clear all credits
     */
    fun clearAllCredits() {
        sharedPreferences.edit()
            .remove(KEY_CARBON_CREDITS)
            .remove(KEY_LAST_BATCH_ID)
            .remove(KEY_PORTFOLIO_STATS)
            .apply()
    }

    /**
     * Get portfolio statistics
     */
    fun getPortfolioStats(): PortfolioStats {
        val credits = loadCredits()

        val totalCredits = credits.sumOf { it.quantity }
        val availableCredits = credits.filter {
            it.status == CreditStatus.AVAILABLE || it.status == CreditStatus.ISSUED
        }.sumOf { it.quantity }
        val retiredCredits = credits.filter {
            it.status == CreditStatus.RETIRED
        }.sumOf { it.quantity }
        val totalValue = credits.sumOf { it.totalValue }

        return PortfolioStats(
            totalCredits = totalCredits,
            availableCredits = availableCredits,
            retiredCredits = retiredCredits,
            totalValue = totalValue,
            creditCount = credits.size
        )
    }

    /**
     * Get the last batch ID for generating new batch IDs
     */
    fun getLastBatchId(): String? {
        return sharedPreferences.getString(KEY_LAST_BATCH_ID, null)
    }

    /**
     * Check if storage has any credits
     */
    fun hasCredits(): Boolean {
        return loadCredits().isNotEmpty()
    }

    /**
     * Get total count of credits
     */
    fun getCreditCount(): Int {
        return loadCredits().size
    }

    /**
     * Generate next batch ID
     */
    fun generateNextBatchId(): String {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentCredits = loadCredits()
        val thisYearCredits = currentCredits.filter {
            it.batchId.contains(currentYear.toString())
        }
        val nextNumber = thisYearCredits.size + 1
        return "BCR-$currentYear-${String.format("%03d", nextNumber)}"
    }
}

/**
 * Data class for portfolio statistics
 */
data class PortfolioStats(
    val totalCredits: Double,
    val availableCredits: Double,
    val retiredCredits: Double,
    val totalValue: Double,
    val creditCount: Int
)
