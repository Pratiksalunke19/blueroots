package com.blueroots.carbonregistry.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.data.models.CreditStatus
import com.blueroots.carbonregistry.databinding.ItemCreditBatchBinding
import java.text.SimpleDateFormat
import java.util.*

class CreditBatchAdapter(
    private val onCreditClick: (CarbonCredit) -> Unit
) : ListAdapter<CarbonCredit, CreditBatchAdapter.CreditViewHolder>(CreditDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditViewHolder {
        val binding = ItemCreditBatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CreditViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CreditViewHolder(
        private val binding: ItemCreditBatchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(credit: CarbonCredit) {
            binding.apply {
                textViewBatchId.text = credit.batchId
                textViewProjectName.text = credit.projectName
                textViewCredits.text = String.format("%.1f tCO2e", credit.quantity)
                textViewPrice.text = String.format("$%.2f/tCO2e", credit.pricePerTonne)
                textViewIssueDate.text = dateFormat.format(credit.issueDate)

                // Set status chip with proper styling
                chipStatus.text = credit.statusDisplayName
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

                // Set button states based on status
                buttonTransfer.isEnabled = credit.status == CreditStatus.AVAILABLE

                buttonViewDetails.setOnClickListener {
                    onCreditClick(credit)
                }

                buttonTransfer.setOnClickListener {
                    // TODO: Implement transfer functionality
                }

                root.setOnClickListener {
                    onCreditClick(credit)
                }
            }
        }
    }

    private class CreditDiffCallback : DiffUtil.ItemCallback<CarbonCredit>() {
        override fun areItemsTheSame(oldItem: CarbonCredit, newItem: CarbonCredit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CarbonCredit, newItem: CarbonCredit): Boolean {
            return oldItem == newItem
        }
    }
}
