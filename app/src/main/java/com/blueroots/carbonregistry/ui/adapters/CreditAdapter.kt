package com.blueroots.carbonregistry.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueroots.carbonregistry.databinding.ItemCreditBinding
import com.blueroots.carbonregistry.data.models.CarbonCredit
import java.text.SimpleDateFormat
import java.util.*

class CreditAdapter : ListAdapter<CarbonCredit, CreditAdapter.CreditViewHolder>(CreditDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditViewHolder {
        val binding = ItemCreditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CreditViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CreditViewHolder(private val binding: ItemCreditBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(credit: CarbonCredit) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            binding.apply {
                textCreditId.text = "ID: ${credit.id}"
                textAmount.text = "${credit.amount} tCO2e"
                textStatus.text = "Status: ${credit.status}"
                textTxHash.text = "Tx: ${credit.blockchainTxHash}"
                textIssuedDate.text = "Issued: ${dateFormat.format(Date(credit.issuedDate))}"
            }
        }
    }
}

class CreditDiffCallback : DiffUtil.ItemCallback<CarbonCredit>() {
    override fun areItemsTheSame(oldItem: CarbonCredit, newItem: CarbonCredit): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CarbonCredit, newItem: CarbonCredit): Boolean {
        return oldItem == newItem
    }
}
