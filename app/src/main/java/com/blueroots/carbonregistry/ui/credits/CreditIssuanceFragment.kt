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
import com.blueroots.carbonregistry.data.storage.PortfolioStats
import com.blueroots.carbonregistry.databinding.FragmentCreditIssuanceBinding
import com.blueroots.carbonregistry.ui.adapters.CreditAdapter
import com.blueroots.carbonregistry.viewmodel.CreditViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class CreditIssuanceFragment : Fragment() {

    private var _binding: FragmentCreditIssuanceBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share the same instance across fragments
    private val viewModel: CreditViewModel by activityViewModels()
    private lateinit var creditAdapter: CreditAdapter

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

        setupRecyclerView()
        setupDropdowns()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        creditAdapter = CreditAdapter(
            onCreditClick = { credit ->
                showCreditDetails(credit)
            },
            onTransferClick = { credit ->
                handleCreditTransfer(credit)
            }
        )

        binding.recyclerViewCredits.apply {
            adapter = creditAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // Add this method to handle transfers
    private fun handleCreditTransfer(credit: CarbonCredit) {
        // Mock transfer completion
        Snackbar.make(
            binding.root,
            "Initiating transfer on Hedera blockchain...",
            Snackbar.LENGTH_LONG
        ).show()

        // Simulate blockchain transaction delay
        binding.root.postDelayed({
            Snackbar.make(
                binding.root,
                "Transfer completed! TX: ${System.currentTimeMillis().toString().takeLast(8)}",
                Snackbar.LENGTH_LONG
            ).show()
        }, 2000)
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
        binding.dropdownProjectFilter.setOnItemClickListener { _, _, _, _ ->
            applyFilters()
        }

        binding.dropdownStatusFilter.setOnItemClickListener { _, _, _, _ ->
            applyFilters()
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            buttonRefresh.setOnClickListener {
                refreshData()
            }

            buttonRequestVerification.setOnClickListener {
                requestVerification()
            }

            buttonExportReport.setOnClickListener {
                exportReport()
            }

            buttonViewMarketplace.setOnClickListener {
                viewMarketplace()
            }
        }
    }

    private fun observeViewModel() {
        // Observe credit list changes - this will automatically update when SharedPreferences change
        viewModel.creditList.observe(viewLifecycleOwner) { credits ->
            creditAdapter.updateCredits(credits)
        }

        // Use filtered credits if filtering is active
        viewModel.filteredCredits.observe(viewLifecycleOwner) { credits ->
            creditAdapter.updateCredits(credits)
            updatePortfolioStats(credits)
        }

        // Observe portfolio stats
        viewModel.portfolioStats.observe(viewLifecycleOwner) { stats ->
            updatePortfolioStatsUI(stats)
        }

        // Observe blockchain status
        viewModel.blockchainStatus.observe(viewLifecycleOwner) { status ->
            if (status.isNotEmpty()) {
                binding.tvBlockchainStatus.text = status
                binding.cardBlockchainStatus.visibility = View.VISIBLE

                // Auto-hide after 5 seconds
                binding.tvBlockchainStatus.postDelayed({
                    binding.cardBlockchainStatus.visibility = View.GONE
                    viewModel.clearBlockchainStatus()
                }, 5000)
            } else {
                binding.cardBlockchainStatus.visibility = View.GONE
            }
        }

        // Observe transaction results
        viewModel.transactionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                showCreditIssuanceSuccess(it)
            }
        }

        // Observe newly issued credits
        viewModel.lastIssuedCredit.observe(viewLifecycleOwner) { newCredit ->
            newCredit?.let {
                showNewCreditNotification(it)
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.buttonRefresh.isEnabled = !isLoading
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun updatePortfolioStatsUI(stats: PortfolioStats) {
        binding.apply {
            textViewTotalCredits.text = String.format("%.1f", stats.totalCredits)
            textViewAvailableCredits.text = String.format("%.1f", stats.availableCredits)
            textViewRetiredCredits.text = String.format("%.1f", stats.retiredCredits)
            textViewRevenue.text = "$${String.format("%,.0f", stats.totalValue)}"
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

    private fun refreshData() {
        Snackbar.make(binding.root, "🔄 Refreshing credit data from Hedera...", Snackbar.LENGTH_SHORT).show()
        viewModel.refreshCarbonCredits()
    }

    private fun applyFilters() {
        val selectedProject = binding.dropdownProjectFilter.text.toString()
        val selectedStatus = binding.dropdownStatusFilter.text.toString()
        viewModel.filterCredits(selectedProject, selectedStatus)
    }

    private fun showCreditDetails(credit: CarbonCredit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Carbon Credit Details")
            .setMessage("""
                Batch ID: ${credit.batchId}
                Project: ${credit.projectName}
                Quantity: ${credit.quantity} tCO2e
                Status: ${credit.status.displayName}
                Location: ${credit.location}
                
                🔗 Blockchain Details:
                Transaction: ${credit.transactionHash ?: "N/A"}
                Verification Hash: ${credit.verificationHash?.take(16)}...
                Network Status: ${credit.blockchainStatus}
            """.trimIndent())
            .setPositiveButton("OK", null)
            .setNeutralButton("View on Hedera") { _, _ ->
                // Mock blockchain explorer view
                Snackbar.make(binding.root, "🔍 Opening Hedera explorer...", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showCreditIssuanceSuccess(result: HederaTransactionResult) {
        Snackbar.make(
            binding.root,
            "✅ Credits issued on Hedera! TX: ${result.transactionId.takeLast(10)}...",
            Snackbar.LENGTH_LONG
        ).setAction("Details") {
            showTransactionDialog(result)
        }.show()
    }

    private fun showNewCreditNotification(credit: CarbonCredit) {
        Snackbar.make(
            binding.root,
            "🎉 New batch created: ${credit.batchId} (${credit.quantity.toInt()} tCO2e)",
            Snackbar.LENGTH_LONG
        ).setAction("View") {
            // Scroll to top to show the new credit
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
                
                Your carbon credits have been successfully recorded on the Hedera blockchain network.
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun requestVerification() {
        Snackbar.make(binding.root, "📋 Verification request submitted to Hedera Guardian", Snackbar.LENGTH_SHORT).show()
    }

    private fun exportReport() {
        Snackbar.make(binding.root, "📊 Exporting blockchain-verified credit report...", Snackbar.LENGTH_SHORT).show()
    }

    private fun viewMarketplace() {
        Snackbar.make(binding.root, "🏪 Opening Hedera-powered carbon marketplace...", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
