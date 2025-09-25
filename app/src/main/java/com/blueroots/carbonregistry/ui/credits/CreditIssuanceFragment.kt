package com.blueroots.carbonregistry.ui.credits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.data.blockchain.HederaTransactionResult
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.data.models.CreditStatus
import com.blueroots.carbonregistry.data.models.MonitoringData
import com.blueroots.carbonregistry.databinding.FragmentCreditIssuanceBinding
import com.blueroots.carbonregistry.ui.adapters.CreditAdapter
import com.blueroots.carbonregistry.ui.adapters.MonitoringDataAdapter
import com.blueroots.carbonregistry.viewmodel.CreditViewModel
import com.blueroots.carbonregistry.viewmodel.MonitoringViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class CreditIssuanceFragment : Fragment() {

    private var _binding: FragmentCreditIssuanceBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share data across fragments
    private val creditViewModel: CreditViewModel by activityViewModels()
    private val monitoringViewModel: MonitoringViewModel by activityViewModels()

    private lateinit var creditAdapter: CreditAdapter
    private lateinit var monitoringAdapter: MonitoringDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditIssuanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupDropdowns()
        setupClickListeners()
        observeViewModels()
    }

    private fun setupRecyclerViews() {
        // Credit adapter
        creditAdapter = CreditAdapter(
            onCreditClick = { credit -> showCreditDetails(credit) },
            onTransferClick = { credit -> handleCreditTransfer(credit) }
        )

        binding.recyclerViewCredits.apply {
            adapter = creditAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Monitoring data adapter (compact version for credits view)
        monitoringAdapter = MonitoringDataAdapter(
            onItemClick = { data -> showMonitoringDetails(data) },
            onVerifyClick = { data -> verifyMonitoringData(data) },
            onDeleteClick = { data -> deleteMonitoringData(data) }
        )

        binding.recyclerViewMonitoring.apply {
            adapter = monitoringAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupDropdowns() {
        // Project Filter
        val projects = listOf(
            "All Projects",
            "Sundarbans Mangrove Restoration",
            "Gulf Coast Blue Carbon Project",
            "Pacific Seagrass Conservation",
            "Coastal Wetland Recovery"
        )
        val projectAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, projects)
        binding.dropdownProjectFilter.setAdapter(projectAdapter)

        // Status Filter
        val statuses = listOf("All Status", "Verified", "Pending Verification", "Issued", "Available", "Retired", "Transferred")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses)
        binding.dropdownStatusFilter.setAdapter(statusAdapter)

        // Setup filter listeners
        binding.dropdownProjectFilter.setOnItemClickListener { _, _, _, _ -> applyFilters() }
        binding.dropdownStatusFilter.setOnItemClickListener { _, _, _, _ -> applyFilters() }
    }

    private fun setupClickListeners() {
        binding.apply {
            buttonRefresh.setOnClickListener { refreshData() }

            buttonRequestVerification.setOnClickListener { requestVerification() }

            buttonExportReport.setOnClickListener { exportReport() }

            buttonViewMarketplace.setOnClickListener { viewMarketplace() }

            // Toggle between credits and monitoring data view
            chipShowCredits.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) showCreditsView() else showMonitoringView()
            }

            chipShowMonitoring.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) showMonitoringView() else showCreditsView()
            }
        }
    }

    private fun observeViewModels() {
        // Observe credit data
        creditViewModel.filteredCredits.observe(viewLifecycleOwner) { credits ->
            creditAdapter.updateCredits(credits)
            updatePortfolioStats(credits)
        }

        // Observe monitoring data
        monitoringViewModel.filteredMonitoringData.observe(viewLifecycleOwner) { monitoringData ->
            monitoringAdapter.updateData(monitoringData)
            updateMonitoringStats(monitoringData)

            // Check if new monitoring data can generate credits
            checkForCreditGeneration(monitoringData)
        }

        // Observe blockchain status
        creditViewModel.blockchainStatus.observe(viewLifecycleOwner) { status ->
            if (status.isNotEmpty()) {
                binding.tvBlockchainStatus.text = status
                binding.cardBlockchainStatus.visibility = View.VISIBLE

                binding.tvBlockchainStatus.postDelayed({
                    binding.cardBlockchainStatus.visibility = View.GONE
                    creditViewModel.clearBlockchainStatus()
                }, 5000)
            } else {
                binding.cardBlockchainStatus.visibility = View.GONE
            }
        }

        // Observe transaction results
        creditViewModel.transactionResult.observe(viewLifecycleOwner) { result ->
            result?.let { showCreditIssuanceSuccess(it) }
        }

        // Observe newly issued credits
        creditViewModel.lastIssuedCredit.observe(viewLifecycleOwner) { newCredit ->
            newCredit?.let { showNewCreditNotification(it) }
        }

        // Observe loading states
        creditViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.buttonRefresh.isEnabled = !isLoading
        }

        // Observe monitoring upload results
        monitoringViewModel.uploadResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is MonitoringViewModel.UploadResult.Success -> {
                    showMonitoringUploadSuccess(result.message)
                }
                is MonitoringViewModel.UploadResult.Error -> {
                    Snackbar.make(binding.root, "Monitoring Error: ${result.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showCreditsView() {
        binding.apply {
            recyclerViewCredits.visibility = View.VISIBLE
            recyclerViewMonitoring.visibility = View.GONE
            textViewCreditsTitle.text = "Carbon Credit Batches"
            chipShowCredits.isChecked = true
            chipShowMonitoring.isChecked = false
        }
    }

    private fun showMonitoringView() {
        binding.apply {
            recyclerViewCredits.visibility = View.GONE
            recyclerViewMonitoring.visibility = View.VISIBLE
            textViewCreditsTitle.text = "Monitoring Data Entries"
            chipShowCredits.isChecked = false
            chipShowMonitoring.isChecked = true
        }
    }

    private fun updatePortfolioStats(credits: List<CarbonCredit>) {
        val totalCredits = credits.sumOf { it.quantity }
        val availableCredits = credits.filter {
            it.status == CreditStatus.AVAILABLE || it.status == CreditStatus.ISSUED
        }.sumOf { it.quantity }
        val retiredCredits = credits.filter { it.status == CreditStatus.RETIRED }.sumOf { it.quantity }
        val totalRevenue = credits.sumOf { it.totalValue }

        binding.apply {
            textViewTotalCredits.text = String.format("%.1f", totalCredits)
            textViewAvailableCredits.text = String.format("%.1f", availableCredits)
            textViewRetiredCredits.text = String.format("%.1f", retiredCredits)
            textViewRevenue.text = "$${String.format("%,.0f", totalRevenue)}"
        }
    }

    private fun updateMonitoringStats(monitoringData: List<MonitoringData>) {
        val totalEntries = monitoringData.size
        val verifiedEntries = monitoringData.count { it.verificationStatus == com.blueroots.carbonregistry.data.models.VerificationStatus.VERIFIED }
        val pendingEntries = monitoringData.count { it.verificationStatus == com.blueroots.carbonregistry.data.models.VerificationStatus.PENDING }
        val recentEntries = monitoringData.count {
            val thirtyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.time
            it.monitoringDate.after(thirtyDaysAgo)
        }

        // Update stats cards to show monitoring info when in monitoring view
        if (binding.chipShowMonitoring.isChecked) {
            binding.apply {
                textViewTotalCredits.text = totalEntries.toString()
                textViewAvailableCredits.text = verifiedEntries.toString()
                textViewRetiredCredits.text = pendingEntries.toString()
                textViewRevenue.text = recentEntries.toString()
            }
        }
    }

    private fun checkForCreditGeneration(monitoringData: List<MonitoringData>) {
        // Check if recent monitoring data can generate new credits
        val recentVerifiedData = monitoringData.filter { data ->
            data.verificationStatus == com.blueroots.carbonregistry.data.models.VerificationStatus.VERIFIED &&
                    System.currentTimeMillis() - data.monitoringDate.time < 24 * 60 * 60 * 1000 // Last 24 hours
        }

        if (recentVerifiedData.isNotEmpty()) {
            showCreditGenerationOpportunity(recentVerifiedData)
        }
    }

    private fun showCreditGenerationOpportunity(verifiedData: List<MonitoringData>) {
        val potentialCredits = verifiedData.sumOf { data ->
            // Calculate potential credits based on monitoring data
            when (data.dataType) {
                com.blueroots.carbonregistry.data.models.MonitoringDataType.SOIL_SAMPLE -> {
                    data.soilData?.organicCarbonContent?.times(2.5) ?: 0.0
                }
                com.blueroots.carbonregistry.data.models.MonitoringDataType.VEGETATION_SURVEY -> {
                    data.vegetationData?.canopyCover?.times(0.8) ?: 0.0
                }
                else -> 5.0 // Default credit potential
            }
        }

        if (potentialCredits > 10.0) { // Only show if significant credits can be generated
            Snackbar.make(
                binding.root,
                "🌱 New monitoring data can generate ${potentialCredits.toInt()} credits!",
                Snackbar.LENGTH_LONG
            ).setAction("Generate") {
                generateCreditsFromMonitoring(verifiedData, potentialCredits)
            }.show()
        }
    }

    private fun generateCreditsFromMonitoring(monitoringData: List<MonitoringData>, creditAmount: Double) {
        val firstData = monitoringData.first()

        creditViewModel.issueCarbonCreditsFromProject(
            projectId = firstData.projectId,
            projectName = firstData.projectName,
            creditsAmount = creditAmount,
            ecosystemType = com.blueroots.carbonregistry.data.models.EcosystemType.MANGROVE, // Default or derive from data
            location = firstData.location.siteDescription
        )

        showCreditsView() // Switch to credits view to show new batch
    }

    private fun showMonitoringUploadSuccess(message: String) {
        Snackbar.make(
            binding.root,
            "📊 $message",
            Snackbar.LENGTH_LONG
        ).setAction("View Impact") {
            showMonitoringView()
        }.show()
    }

    private fun refreshData() {
        creditViewModel.refreshCarbonCredits()
        monitoringViewModel.clearFilters()
        Snackbar.make(binding.root, "🔄 Data refreshed", Snackbar.LENGTH_SHORT).show()
    }

    private fun applyFilters() {
        val selectedProject = binding.dropdownProjectFilter.text.toString()
        val selectedStatus = binding.dropdownStatusFilter.text.toString()

        // Apply filters to both credit and monitoring data
        creditViewModel.filterCredits(selectedProject, selectedStatus)
        // Add monitoring filter if needed
    }

    private fun showMonitoringDetails(data: MonitoringData) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📊 Monitoring Data Details")
            .setMessage("""
                📋 Basic Information
                ID: ${data.id.take(8)}...
                Project: ${data.projectName}
                Type: ${data.dataType.displayName}
                Date: ${dateFormat.format(data.monitoringDate)}
                
                📍 Location
                Site: ${data.location.siteDescription}
                Coordinates: ${data.location.latitude}, ${data.location.longitude}
                
                👤 Collection Details
                Collector: ${data.dataCollector}
                Equipment: ${data.equipmentUsed.joinToString(", ")}
                
                📊 Data Summary
                ${getDataSummary(data)}
                
                ✅ Status
                Verification: ${data.verificationStatus.name}
                Priority: ${data.priority.name}
                
                🔗 Blockchain Impact
                This data contributes to carbon credit calculations and verification.
            """.trimIndent())
            .setPositiveButton("OK", null)
            .setNeutralButton("Generate Credits") { _, _ ->
                if (data.verificationStatus == com.blueroots.carbonregistry.data.models.VerificationStatus.VERIFIED) {
                    generateCreditsFromMonitoring(listOf(data), calculatePotentialCredits(data))
                } else {
                    Snackbar.make(binding.root, "Data must be verified first", Snackbar.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun getDataSummary(data: MonitoringData): String {
        return when (data.dataType) {
            com.blueroots.carbonregistry.data.models.MonitoringDataType.SOIL_SAMPLE -> {
                data.soilData?.let { soil ->
                    "Organic Carbon: ${soil.organicCarbonContent}%\nPH: ${soil.pH}\nSalinity: ${soil.salinity} ppt"
                } ?: "Soil sample collected"
            }
            com.blueroots.carbonregistry.data.models.MonitoringDataType.VEGETATION_SURVEY -> {
                data.vegetationData?.let { veg ->
                    "Canopy Cover: ${veg.canopyCover}%\nHeight: ${veg.averageHeight}m\nHealth: ${veg.healthAssessment.name}"
                } ?: "Vegetation survey completed"
            }
            else -> "${data.dataType.displayName} data collected"
        }
    }

    private fun calculatePotentialCredits(data: MonitoringData): Double {
        return when (data.dataType) {
            com.blueroots.carbonregistry.data.models.MonitoringDataType.SOIL_SAMPLE -> {
                data.soilData?.organicCarbonContent?.times(2.5) ?: 10.0
            }
            com.blueroots.carbonregistry.data.models.MonitoringDataType.VEGETATION_SURVEY -> {
                data.vegetationData?.canopyCover?.times(0.8) ?: 15.0
            }
            else -> 8.0
        }
    }

    private fun verifyMonitoringData(data: MonitoringData) {
        monitoringViewModel.updateVerificationStatus(
            data.id,
            com.blueroots.carbonregistry.data.models.VerificationStatus.VERIFIED,
            "Verified via Carbon Credits interface"
        )
    }

    private fun deleteMonitoringData(data: MonitoringData) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Monitoring Data")
            .setMessage("Are you sure you want to delete this monitoring entry?")
            .setPositiveButton("Delete") { _, _ ->
                monitoringViewModel.deleteMonitoringData(data.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCreditDetails(credit: CarbonCredit) {
        // Your existing credit details implementation
    }

    private fun handleCreditTransfer(credit: CarbonCredit) {
        // Your existing credit transfer implementation
    }

    private fun showCreditIssuanceSuccess(result: HederaTransactionResult) {
        Snackbar.make(
            binding.root,
            "✅ Credits issued! TX: ${result.transactionId.takeLast(10)}...",
            Snackbar.LENGTH_LONG
        ).setAction("Details") {
            showTransactionDialog(result)
        }.show()
    }

    private fun showNewCreditNotification(credit: CarbonCredit) {
        Snackbar.make(
            binding.root,
            "🎉 New batch: ${credit.batchId} (${credit.quantity.toInt()} tCO2e)",
            Snackbar.LENGTH_LONG
        ).setAction("View") {
            binding.recyclerViewCredits.smoothScrollToPosition(0)
        }.show()
    }

    private fun showTransactionDialog(result: HederaTransactionResult) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🔗 Hedera Transaction Success")
            .setMessage("""
                Transaction ID: ${result.transactionId}
                Consensus Time: ${result.consensusTimestamp}
                Status: ${result.status}
                Network Fee: ${result.fee}
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun requestVerification() {
        Snackbar.make(binding.root, "📋 Verification request submitted", Snackbar.LENGTH_SHORT).show()
    }

    private fun exportReport() {
        Snackbar.make(binding.root, "📊 Exporting report...", Snackbar.LENGTH_SHORT).show()
    }

    private fun viewMarketplace() {
        Snackbar.make(binding.root, "🏪 Opening marketplace...", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
