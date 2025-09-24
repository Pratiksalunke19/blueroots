package com.blueroots.carbonregistry.data.blockchain

import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

data class HederaTransactionResult(
    val transactionId: String,
    val consensusTimestamp: String,
    val status: String,
    val accountId: String = "0.0.1001", // Mock payer account
    val fee: String,
    val memo: String? = null
)

data class CarbonCreditBatch(
    val batchId: String,
    val projectId: String,
    val credits: Int,
    val transactionId: String,
    val consensusTimestamp: String,
    val status: String,
    val verificationHash: String
)

class MockHederaService {
    private val mockAccount = "0.0.1001" // Mock BlueRoots account ID
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    // Simulate network delay
    private suspend fun simulateNetworkDelay() {
        delay(Random.nextLong(1000, 3000)) // 1-3 seconds delay
    }

    /**
     * Simulate project registration on Hedera network
     */
    suspend fun registerProject(projectData: Map<String, Any>): HederaTransactionResult {
        simulateNetworkDelay()

        val timestamp = System.currentTimeMillis()
        val nanos = String.format("%09d", Random.nextLong(100000000, 999999999))
        val transactionId = "$mockAccount@${timestamp / 1000}.${nanos}"
        val consensusTimestamp = dateFormat.format(Date(timestamp))

        return HederaTransactionResult(
            transactionId = transactionId,
            consensusTimestamp = consensusTimestamp,
            status = "SUCCESS",
            fee = "${Random.nextDouble(0.05, 0.15)} HBAR",
            memo = "PROJECT_REGISTRATION"
        )
    }

    /**
     * Simulate monitoring data submission to Hedera
     */
    suspend fun submitMonitoringData(
        projectId: String,
        monitoringData: Map<String, Any>
    ): HederaTransactionResult {
        simulateNetworkDelay()

        val timestamp = System.currentTimeMillis()
        val nanos = String.format("%09d", Random.nextLong(100000000, 999999999))
        val transactionId = "$mockAccount@${timestamp / 1000}.${nanos}"
        val consensusTimestamp = dateFormat.format(Date(timestamp))

        return HederaTransactionResult(
            transactionId = transactionId,
            consensusTimestamp = consensusTimestamp,
            status = "SUCCESS",
            fee = "${Random.nextDouble(0.02, 0.08)} HBAR",
            memo = "MONITORING_DATA_${projectId}"
        )
    }

    /**
     * Simulate carbon credit batch issuance on Hedera
     */
    suspend fun issueCarbonCreditBatch(
        projectId: String,
        creditsAmount: Int,
        verificationData: Map<String, Any>
    ): CarbonCreditBatch {
        simulateNetworkDelay()

        val timestamp = System.currentTimeMillis()
        val nanos = String.format("%09d", Random.nextLong(100000000, 999999999))
        val transactionId = "$mockAccount@${timestamp / 1000}.${nanos}"
        val consensusTimestamp = dateFormat.format(Date(timestamp))
        val batchId = "BCR-${projectId.takeLast(4)}-${Random.nextInt(1000, 9999)}"
        val verificationHash = generateMockHash()

        return CarbonCreditBatch(
            batchId = batchId,
            projectId = projectId,
            credits = creditsAmount,
            transactionId = transactionId,
            consensusTimestamp = consensusTimestamp,
            status = "ISSUED",
            verificationHash = verificationHash
        )
    }

    /**
     * Simulate querying transaction status
     */
    suspend fun getTransactionStatus(transactionId: String): String {
        delay(500) // Quick status check
        return if (Random.nextDouble() > 0.05) "SUCCESS" else "PENDING" // 95% success rate
    }

    /**
     * Generate mock blockchain verification hash
     */
    private fun generateMockHash(): String {
        val chars = "0123456789abcdef"
        return (1..64)
            .map { chars.random() }
            .joinToString("")
    }

    /**
     * Simulate getting network fees
     */
    suspend fun getNetworkFees(): Map<String, String> {
        delay(200)
        return mapOf(
            "projectRegistration" to "0.10 HBAR",
            "monitoringData" to "0.05 HBAR",
            "creditIssuance" to "0.15 HBAR",
            "transfer" to "0.0001 HBAR"
        )
    }
}
