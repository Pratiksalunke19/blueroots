package com.blueroots.carbonregistry.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.data.models.CreditStatus
import com.blueroots.carbonregistry.databinding.ItemCreditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class CreditAdapter(
    private val onCreditClick: (CarbonCredit) -> Unit,
    private val onTransferClick: ((CarbonCredit) -> Unit)? = null
) : RecyclerView.Adapter<CreditAdapter.CreditViewHolder>() {

    private var credits = listOf<CarbonCredit>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditViewHolder {
        val binding = ItemCreditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CreditViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        holder.bind(credits[position])
    }

    override fun getItemCount(): Int = credits.size

    /**
     * Update the credits list and refresh the RecyclerView
     */
    fun updateCredits(newCredits: List<CarbonCredit>) {
        credits = newCredits
        notifyDataSetChanged()
    }

    inner class CreditViewHolder(
        private val binding: ItemCreditBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(credit: CarbonCredit) {
            binding.apply {
                // Basic credit information
                textCreditId.text = credit.batchId
                textViewProjectName.text = credit.projectName
                textAmount.text = "${credit.quantity.toInt()} tCO2e"
                textPrice.text = "$${String.format("%.2f", credit.pricePerTonne)}/tCO2e"
                textIssuedDate.text = "Issued: ${dateFormat.format(credit.issueDate)}"

                // Status chip styling
                chipStatus.text = credit.status.displayName
                when (credit.status) {
                    CreditStatus.VERIFIED, CreditStatus.ISSUED, CreditStatus.AVAILABLE -> {
                        chipStatus.setChipBackgroundColorResource(R.color.status_ready)
                        chipStatus.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
                    }
                    CreditStatus.PENDING_VERIFICATION -> {
                        chipStatus.setChipBackgroundColorResource(R.color.status_warning)
                        chipStatus.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
                    }
                    CreditStatus.RETIRED, CreditStatus.CANCELLED -> {
                        chipStatus.setChipBackgroundColorResource(R.color.status_error)
                        chipStatus.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
                    }
                    else -> {
                        chipStatus.setChipBackgroundColorResource(android.R.color.darker_gray)
                        chipStatus.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
                    }
                }

                // Blockchain-specific information
                tvBlockchainStatus.text = when (credit.blockchainStatus) {
                    "VERIFIED_ON_HEDERA" -> "✓ Verified on Hedera"
                    "PENDING_ON_HEDERA" -> "⏳ Pending on Hedera"
                    "RETIRED_ON_HEDERA" -> "♻ Retired on Hedera"
                    "ISSUED_ON_HEDERA" -> "📋 Issued on Hedera"
                    else -> "🔗 On Hedera Network"
                }

                // Transaction hash display
                tvTransactionHash.text = credit.transactionHash?.let { hash ->
                    "TX: ${hash.takeLast(12)}..."
                } ?: "TX: Processing..."

                // Legacy fields (backward compatibility)
                textStatus.text = "Status: ${credit.status.displayName}"
                textTxHash.text = credit.transactionHash?.let { hash ->
                    "Tx: ${hash.takeLast(8)}..."
                } ?: "Tx: Pending"

                // Button setup
                setupButtons(credit)
            }
        }

        private fun setupButtons(credit: CarbonCredit) {
            binding.apply {
                // View Details Button
                buttonViewDetails.setOnClickListener {
                    showCreditDetails(credit)
                }

                // Transfer Button
                buttonTransfer.isEnabled = credit.status == CreditStatus.AVAILABLE
                buttonTransfer.setOnClickListener {
                    if (credit.status == CreditStatus.AVAILABLE) {
                        showTransferDialog(credit)
                    } else {
                        Snackbar.make(
                            root,
                            "Only available credits can be transferred",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }

                // Card click (original functionality)
                root.setOnClickListener {
                    onCreditClick(credit)
                }
            }
        }

        private fun showCreditDetails(credit: CarbonCredit) {
            val context = binding.root.context
            MaterialAlertDialogBuilder(context)
                .setTitle("Carbon Credit Details")
                .setMessage("""
                    📋 Batch Information
                    Batch ID: ${credit.batchId}
                    Project: ${credit.projectName}
                    Location: ${credit.location}
                    
                    💰 Financial Details
                    Quantity: ${credit.quantity} tCO2e
                    Price: $${String.format("%.2f", credit.pricePerTonne)}/tCO2e
                    Total Value: $${String.format("%.2f", credit.totalValue)}
                    
                    📊 Status Information
                    Status: ${credit.status.displayName}
                    Issued: ${dateFormat.format(credit.issueDate)}
                    Vintage Year: ${credit.vintageYear}
                    
                    🔗 Blockchain Details
                    Transaction: ${credit.transactionHash ?: "N/A"}
                    Verification Hash: ${credit.verificationHash?.take(16)}...
                    Network Status: ${credit.blockchainStatus}
                    
                    🌱 Environmental Details
                    Ecosystem: ${credit.ecosystemType}
                    Methodology: ${credit.methodology}
                    Standard: ${credit.standard}
                    Registry: ${credit.registry}
                    Verification Body: ${credit.verificationBody}
                    ${if (credit.retirementDate != null) "\n♻️ Retired: ${dateFormat.format(credit.retirementDate)}" else ""}
                    ${if (credit.buyer != null) "Buyer: ${credit.buyer}" else ""}
                """.trimIndent())
                .setPositiveButton("OK", null)
                .setNeutralButton("View on Hedera") { _, _ ->
                    Snackbar.make(
                        binding.root,
                        "🔍 Opening Hedera Explorer...",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                .show()
        }

        private fun showTransferDialog(credit: CarbonCredit) {
            val context = binding.root.context
            MaterialAlertDialogBuilder(context)
                .setTitle("Transfer Carbon Credits")
                .setMessage("""
                    Transfer ${credit.quantity} tCO2e from batch ${credit.batchId}?
                    
                    This will initiate a blockchain transaction on Hedera network.
                    
                    Estimated fee: 0.0001 HBAR
                """.trimIndent())
                .setPositiveButton("Transfer") { _, _ ->
                    // Mock transfer functionality
                    Snackbar.make(
                        binding.root,
                        "🔄 Initiating transfer on Hedera blockchain...",
                        Snackbar.LENGTH_LONG
                    ).show()

                    // Call transfer callback if provided
                    onTransferClick?.invoke(credit)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
