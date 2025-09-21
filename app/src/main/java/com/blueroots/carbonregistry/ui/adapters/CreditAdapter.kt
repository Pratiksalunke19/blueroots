package com.blueroots.carbonregistry.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.databinding.ItemCreditBinding
import java.text.SimpleDateFormat
import java.util.*

class CreditAdapter(
    private val credits: List<CarbonCredit>,
    private val onCreditClick: (CarbonCredit) -> Unit
) : RecyclerView.Adapter<CreditAdapter.CreditViewHolder>() {

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

    inner class CreditViewHolder(
        private val binding: ItemCreditBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(credit: CarbonCredit) {
            binding.apply {
                // FIXED: Using correct field names from CarbonCredit model
                textCreditId.text = "ID: ${credit.batchId}" // Using batchId instead of id
                textAmount.text = "${credit.quantity} tCO2e" // Using quantity instead of amount
                textStatus.text = "Status: ${credit.statusDisplayName}" // Using statusDisplayName

                // For blockchain transaction hash - using serial numbers or a placeholder
                textTxHash.text = if (credit.serialNumbers.isNotEmpty()) {
                    "Serial: ${credit.serialNumbers.first()}"
                } else {
                    "Serial: Not assigned"
                }

                // FIXED: Using issueDate instead of issuedDate
                textIssuedDate.text = "Issued: ${dateFormat.format(credit.issueDate)}"

                // Handle click
                root.setOnClickListener {
                    onCreditClick(credit)
                }
            }
        }
    }
}
