package com.blueroots.carbonregistry.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.data.models.*
import com.blueroots.carbonregistry.databinding.ItemMonitoringDataBinding
import java.text.SimpleDateFormat
import java.util.*

class MonitoringDataAdapter(
    private val onItemClick: (MonitoringData) -> Unit,
    private val onVerifyClick: (MonitoringData) -> Unit,
    private val onDeleteClick: (MonitoringData) -> Unit
) : RecyclerView.Adapter<MonitoringDataAdapter.MonitoringViewHolder>() {

    private var monitoringData = listOf<MonitoringData>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonitoringViewHolder {
        val binding = ItemMonitoringDataBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MonitoringViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonitoringViewHolder, position: Int) {
        holder.bind(monitoringData[position])
    }

    override fun getItemCount() = monitoringData.size

    fun updateData(newData: List<MonitoringData>) {
        monitoringData = newData
        notifyDataSetChanged()
    }

    inner class MonitoringViewHolder(
        private val binding: ItemMonitoringDataBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: MonitoringData) {
            binding.apply {
                // Basic information
                textMonitoringId.text = data.id.takeLast(8)
                textProjectName.text = data.projectName
                textDataType.text = data.dataType.displayName
                textMonitoringDate.text = dateFormat.format(data.monitoringDate)
                textCollector.text = "Collected by: ${data.dataCollector}"

                // Location
                textLocation.text = if (data.location.siteDescription.isNotEmpty()) {
                    data.location.siteDescription
                } else {
                    "${data.location.latitude}, ${data.location.longitude}"
                }

                // Verification status chip
                chipVerificationStatus.text = data.verificationStatus.name
                when (data.verificationStatus) {
                    VerificationStatus.VERIFIED -> {
                        chipVerificationStatus.setChipBackgroundColorResource(R.color.status_ready)
                        chipVerificationStatus.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
                    }
                    VerificationStatus.PENDING -> {
                        chipVerificationStatus.setChipBackgroundColorResource(R.color.status_warning)
                        chipVerificationStatus.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
                    }
                    VerificationStatus.REJECTED -> {
                        chipVerificationStatus.setChipBackgroundColorResource(R.color.status_error)
                        chipVerificationStatus.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
                    }
                    else -> {
                        chipVerificationStatus.setChipBackgroundColorResource(android.R.color.darker_gray)
                        chipVerificationStatus.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
                    }
                }

                // Priority indicator
                indicatorPriority.setBackgroundColor(
                    ContextCompat.getColor(root.context, when (data.priority) {
                        Priority.URGENT -> R.color.status_error
                        Priority.HIGH -> R.color.status_warning
                        Priority.MEDIUM -> R.color.status_ready
                        Priority.LOW -> android.R.color.darker_gray
                    })
                )

                // Sync status
                iconSyncStatus.setImageResource(when (data.syncStatus) {
                    SyncStatus.SYNCED -> R.drawable.ic_check_circle_24
                    SyncStatus.SYNCING -> R.drawable.ic_sync_24
                    SyncStatus.LOCAL -> R.drawable.ic_smartphone_24
                    SyncStatus.ERROR -> R.drawable.ic_error_24
                })

                // Data summary based on type
                textDataSummary.text = getDataSummary(data)

                // Button setup
                buttonVerify.isEnabled = data.verificationStatus != VerificationStatus.VERIFIED
                buttonVerify.setOnClickListener { onVerifyClick(data) }

                buttonDelete.setOnClickListener { onDeleteClick(data) }

                // Card click
                root.setOnClickListener { onItemClick(data) }
            }
        }

        private fun getDataSummary(data: MonitoringData): String {
            return when (data.dataType) {
                MonitoringDataType.SOIL_SAMPLE -> {
                    data.soilData?.let { soil ->
                        "Carbon: ${soil.organicCarbonContent}% | pH: ${soil.pH} | Stock: ${soil.carbonStock} tC/ha"
                    } ?: "Soil sample data"
                }
                MonitoringDataType.VEGETATION_SURVEY -> {
                    data.vegetationData?.let { veg ->
                        "Cover: ${veg.canopyCover}% | Height: ${veg.averageHeight}m | Health: ${veg.healthAssessment.name}"
                    } ?: "Vegetation survey data"
                }
                MonitoringDataType.WATER_QUALITY -> {
                    data.hydrologyData?.let { hydro ->
                        "Temp: ${hydro.waterTemperature}°C | Salinity: ${hydro.salinity} ppt | pH: ${hydro.pH}"
                    } ?: "Water quality data"
                }
                MonitoringDataType.CARBON_MEASUREMENT -> {
                    data.carbonData?.let { carbon ->
                        "Stock: ${carbon.totalCarbonStock} tC/ha | CO2e: ${carbon.totalCO2Equivalent} tCO2e/ha"
                    } ?: "Carbon measurement data"
                }
                else -> "${data.dataType.displayName} collected"
            }
        }
    }
}
