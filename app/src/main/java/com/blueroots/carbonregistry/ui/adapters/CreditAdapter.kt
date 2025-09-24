package com.blueroots.carbonregistry.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.databinding.ItemCreditBinding
import java.text.SimpleDateFormat
import java.util.*

class CreditAdapter(
    private val onCreditClick: (CarbonCredit) -> Unit
) : RecyclerView.Adapter<CreditAdapter.CreditViewHolder>() {

    private var credits = listOf<CarbonCredit>()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

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
                textCreditId.text = "ID: ${credit.batchId}"
                textAmount.text = "${credit.quantity} tCO2e"
                textStatus.text = "Status: ${credit.status}"
                textIssuedDate.text = "Issued: ${dateFormat.format(credit.issueDate)}"

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

                // Legacy transaction hash (for backward compatibility)
                textTxHash.text = credit.transactionHash?.let { hash ->
                    "Tx: ${hash.takeLast(8)}..."
                } ?: "Tx: Pending"

                // Handle click
                root.setOnClickListener {
                    onCreditClick(credit)
                }
            }
        }
    }
}
