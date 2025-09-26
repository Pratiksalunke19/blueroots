package com.blueroots.carbonregistry.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
    private val onTransferClick: (CarbonCredit) -> Unit,
    private val onPredictFutureClick: (CarbonCredit) -> Unit // NEW callback
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
        val credit = credits[position]
        holder.bind(credit)

        // Handle Oracle button after binding
        holder.binding.apply {
            val isIssued = credit.status == CreditStatus.ISSUED || credit.status == CreditStatus.AVAILABLE

            // Show/hide Oracle button based on credit status (try-catch for safety)
            try {
                buttonPredictFuture.visibility = if (isIssued) View.VISIBLE else View.GONE

                // Set click listener
                buttonPredictFuture.setOnClickListener { onPredictFutureClick(credit) }

                // Add animation if issued
                if (isIssued) {
                    try {
                        val pulseAnimation = AnimationUtils.loadAnimation(root.context, R.anim.pulse_animation)
                        buttonPredictFuture.startAnimation(pulseAnimation)
                    } catch (e: Exception) {
                        // Animation not found, skip
                    }
                }
            } catch (e: Exception) {
                // buttonPredictFuture doesn't exist in layout, skip
            }
        }
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
        val binding: ItemCreditBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(credit: CarbonCredit) {
            binding.apply {
                // Basic credit information
                textCreditId.text = credit.batchId
                textViewProjectName.text = credit.projectName
                textAmount.text = "${credit.quantity.toInt()} tCO2e"
                textPrice.text = "$${String.format("%.2f", credit.pricePerTonne)}/tCO2e"
                textIssuedDate.text = "Issued: ${dateFormat.format(credit.issueDate)}"

                // Set main click listener
                root.setOnClickListener { onCreditClick(credit) }

                // Set button click listeners with try-catch for optional buttons
                try {
                    buttonViewDetails.setOnClickListener { onCreditClick(credit) }
                } catch (e: Exception) {
                    // buttonViewDetails doesn't exist in layout
                }

                try {
                    buttonTransfer.setOnClickListener { onTransferClick(credit) }
                } catch (e: Exception) {
                    // buttonTransfer doesn't exist in layout
                }

                // Status chip styling
                try {
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
                } catch (e: Exception) {
                    // chipStatus doesn't exist in layout
                }

                // Blockchain-specific information
                try {
                    tvBlockchainStatus.text = when (credit.blockchainStatus) {
                        "VERIFIED_ON_HEDERA" -> "✓ Verified on Hedera"
                        "PENDING_ON_HEDERA" -> "⏳ Pending on Hedera"
                        "RETIRED_ON_HEDERA" -> "♻ Retired on Hedera"
                        "ISSUED_ON_HEDERA" -> "📋 Issued on Hedera"
                        else -> "🔗 On Hedera Network"
                    }
                } catch (e: Exception) {
                    // tvBlockchainStatus doesn't exist in layout
                }

                // Transaction hash display
                try {
                    tvTransactionHash.text = credit.transactionHash?.let { hash ->
                        "TX: ${hash.takeLast(12)}..."
                    } ?: "TX: Processing..."
                } catch (e: Exception) {
                    // tvTransactionHash doesn't exist in layout
                }

                // Legacy fields (backward compatibility)
                try {
                    textStatus.text = "Status: ${credit.status.displayName}"
                } catch (e: Exception) {
                    // textStatus doesn't exist in layout
                }

                try {
                    textTxHash.text = credit.transactionHash?.let { hash ->
                        "Tx: ${hash.takeLast(8)}..."
                    } ?: "Tx: Pending"
                } catch (e: Exception) {
                    // textTxHash doesn't exist in layout
                }

                // Button setup
                setupButtons(credit)
            }
        }

        private fun setupButtons(credit: CarbonCredit) {
            binding.apply {
                // View Details Button
                try {
                    buttonViewDetails.setOnClickListener {
                        showCreditDetails(credit)
                    }
                } catch (e: Exception) {
                    // buttonViewDetails doesn't exist
                }

                // Transfer Button
                try {
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
                } catch (e: Exception) {
                    // buttonTransfer doesn't exist
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

                    // Call transfer callback
                    onTransferClick(credit)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
