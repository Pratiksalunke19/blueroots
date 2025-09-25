package com.blueroots.carbonregistry.ui.monitoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.data.models.*
import com.blueroots.carbonregistry.data.storage.MonitoringStats
import com.blueroots.carbonregistry.databinding.FragmentMonitoringListBinding
import com.blueroots.carbonregistry.ui.adapters.MonitoringDataAdapter
import com.blueroots.carbonregistry.viewmodel.MonitoringViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class MonitoringListFragment : Fragment() {

    private var _binding: FragmentMonitoringListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MonitoringViewModel by activityViewModels()
    private lateinit var monitoringAdapter: MonitoringDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitoringListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        monitoringAdapter = MonitoringDataAdapter(
            onItemClick = { data -> showMonitoringDetails(data) },
            onVerifyClick = { data -> showVerificationDialog(data) },
            onDeleteClick = { data -> showDeleteDialog(data) }
        )

        binding.recyclerViewMonitoring.apply {
            adapter = monitoringAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupFilters() {
        // Data Type Filter
        val dataTypes = listOf("All Types") + MonitoringDataType.values().map { it.displayName }
        val dataTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, dataTypes)
        binding.dropdownDataType.setAdapter(dataTypeAdapter)

        // Verification Status Filter
        val verificationStatuses = listOf("All Status") + VerificationStatus.values().map { it.name }
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, verificationStatuses)
        binding.dropdownVerificationStatus.setAdapter(statusAdapter)

        // Project Filter
        val projects = listOf("All Projects", "Sundarbans Mangrove Restoration", "Gulf Coast Blue Carbon Project", "Pacific Seagrass Conservation")
        val projectAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, projects)
        binding.dropdownProject.setAdapter(projectAdapter)

        // Filter listeners
        binding.dropdownDataType.setOnItemClickListener { _, _, _, _ -> applyFilters() }
        binding.dropdownVerificationStatus.setOnItemClickListener { _, _, _, _ -> applyFilters() }
        binding.dropdownProject.setOnItemClickListener { _, _, _, _ -> applyFilters() }
    }

    private fun setupClickListeners() {
        binding.apply {
            buttonRefresh.setOnClickListener {
                refreshData()
            }

            buttonClearFilters.setOnClickListener {
                clearFilters()
            }

            buttonSyncData.setOnClickListener {
                syncData()
            }

            fabAddMonitoring.setOnClickListener {
                // Navigate to MonitoringUploadFragment
                // This depends on your navigation setup
                Snackbar.make(binding.root, "Navigate to monitoring upload", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        // Observe filtered monitoring data
        viewModel.filteredMonitoringData.observe(viewLifecycleOwner) { data ->
            monitoringAdapter.updateData(data)
            updateStatsCards(data)
        }

        // Observe monitoring statistics
        viewModel.monitoringStats.observe(viewLifecycleOwner) { stats ->
            updateOverallStats(stats)
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonRefresh.isEnabled = !isLoading
        }

        // Observe sync status
        viewModel.syncStatus.observe(viewLifecycleOwner) { status ->
            updateSyncStatus(status)
        }

        // Observe upload results for feedback
        viewModel.uploadResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is MonitoringViewModel.UploadResult.Success -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
                is MonitoringViewModel.UploadResult.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(resources.getColor(R.color.status_error, null))
                        .show()
                }
            }
        }
    }

    private fun applyFilters() {
        val selectedDataType = binding.dropdownDataType.text.toString()
        val selectedStatus = binding.dropdownVerificationStatus.text.toString()
        val selectedProject = binding.dropdownProject.text.toString()

        val dataType = if (selectedDataType != "All Types") {
            MonitoringDataType.values().find { it.displayName == selectedDataType }
        } else null

        val verificationStatus = if (selectedStatus != "All Status") {
            VerificationStatus.valueOf(selectedStatus)
        } else null

        val projectName = if (selectedProject != "All Projects") selectedProject else ""

        viewModel.filterMonitoringData(projectName, dataType, verificationStatus)
    }

    private fun clearFilters() {
        binding.dropdownDataType.setText("All Types", false)
        binding.dropdownVerificationStatus.setText("All Status", false)
        binding.dropdownProject.setText("All Projects", false)
        viewModel.clearFilters()
    }

    private fun refreshData() {
        viewModel.clearFilters() // This will reload all data
        Snackbar.make(binding.root, "🔄 Monitoring data refreshed", Snackbar.LENGTH_SHORT).show()
    }

    private fun syncData() {
        viewModel.syncOfflineData()
    }

    private fun updateStatsCards(data: List<MonitoringData>) {
        val total = data.size
        val verified = data.count { it.verificationStatus == VerificationStatus.VERIFIED }
        val pending = data.count { it.verificationStatus == VerificationStatus.PENDING }
        val urgent = data.count { it.priority == Priority.URGENT }

        binding.apply {
            textTotalEntries.text = total.toString()
            textVerifiedEntries.text = verified.toString()
            textPendingEntries.text = pending.toString()
            textUrgentEntries.text = urgent.toString()
        }
    }

    private fun updateOverallStats(stats: MonitoringStats) {
        binding.apply {
            textTotalProjects.text = stats.projectCount.toString()
            textSyncPending.text = stats.pendingSync.toString()
        }
    }

    private fun updateSyncStatus(status: SyncStatus) {
        val statusText = when (status) {
            SyncStatus.SYNCED -> "✅ Synced"
            SyncStatus.SYNCING -> "🔄 Syncing..."
            SyncStatus.LOCAL -> "📱 Local Only"
            SyncStatus.ERROR -> "❌ Sync Error"
        }
        binding.textSyncStatus.text = statusText
    }

    private fun showMonitoringDetails(data: MonitoringData) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📊 Monitoring Data Details")
            .setMessage(formatMonitoringDetails(data))
            .setPositiveButton("OK", null)
            .setNeutralButton("Edit") { _, _ ->
                // Navigate to edit screen
                Snackbar.make(binding.root, "Edit functionality not implemented", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun formatMonitoringDetails(data: MonitoringData): String {
        return """
            📋 Basic Information
            ID: ${data.id}
            Project: ${data.projectName}
            Data Type: ${data.dataType.displayName}
            Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(data.monitoringDate)}
            
            📍 Location
            Coordinates: ${data.location.latitude}, ${data.location.longitude}
            Site: ${data.location.siteDescription}
            
            👤 Collection Details
            Collector: ${data.dataCollector}
            Qualifications: ${data.collectorQualifications}
            Equipment: ${data.equipmentUsed.joinToString(", ")}
            
            📊 Data Summary
            ${formatDataSummary(data)}
            
            ✅ Status Information
            Verification: ${data.verificationStatus.name}
            Priority: ${data.priority.name}
            Sync Status: ${data.syncStatus.name}
            
            📝 Notes
            ${data.notes}
        """.trimIndent()
    }

    private fun formatDataSummary(data: MonitoringData): String {
        return when (data.dataType) {
            MonitoringDataType.SOIL_SAMPLE -> {
                data.soilData?.let { soil ->
                    "Organic Carbon: ${soil.organicCarbonContent}%\nPH: ${soil.pH}\nSalinity: ${soil.salinity} ppt\nCarbon Stock: ${soil.carbonStock} tC/ha"
                } ?: "No soil data available"
            }
            MonitoringDataType.VEGETATION_SURVEY -> {
                data.vegetationData?.let { veg ->
                    "Canopy Cover: ${veg.canopyCover}%\nAverage Height: ${veg.averageHeight}m\nStem Density: ${veg.stemDensity}/ha\nHealth: ${veg.healthAssessment.name}"
                } ?: "No vegetation data available"
            }
            MonitoringDataType.WATER_QUALITY -> {
                data.hydrologyData?.let { hydro ->
                    "Temperature: ${hydro.waterTemperature}°C\nSalinity: ${hydro.salinity} ppt\nPH: ${hydro.pH}\nDissolved O2: ${hydro.dissolvedOxygen} mg/L"
                } ?: "No hydrology data available"
            }
            else -> "Data collected for ${data.dataType.displayName}"
        }
    }

    private fun showVerificationDialog(data: MonitoringData) {
        val statuses = VerificationStatus.values()
        val statusNames = statuses.map { it.name }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Verification Status")
            .setSingleChoiceItems(statusNames, statuses.indexOf(data.verificationStatus)) { dialog, which ->
                val newStatus = statuses[which]
                viewModel.updateVerificationStatus(data.id, newStatus, "Updated via mobile app")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(data: MonitoringData) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Monitoring Data")
            .setMessage("Are you sure you want to delete this monitoring data entry?\n\nID: ${data.id}\nProject: ${data.projectName}")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMonitoringData(data.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
