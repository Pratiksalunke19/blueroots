package com.blueroots.carbonregistry.ui.credits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.data.models.CreditStatus
import com.blueroots.carbonregistry.databinding.FragmentCreditIssuanceBinding
import com.blueroots.carbonregistry.ui.adapters.CreditBatchAdapter
import com.blueroots.carbonregistry.viewmodel.CreditViewModel
import com.google.android.material.snackbar.Snackbar

class CreditIssuanceFragment : Fragment() {

    private var _binding: FragmentCreditIssuanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreditViewModel by viewModels()
    private lateinit var creditAdapter: CreditBatchAdapter

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
        creditAdapter = CreditBatchAdapter { credit ->
            showCreditDetails(credit)
        }

        binding.recyclerViewCredits.apply {
            adapter = creditAdapter
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
        viewModel.creditList.observe(viewLifecycleOwner) { credits ->
            creditAdapter.submitList(credits)
            updateSummaryCards(credits)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.buttonRefresh.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun refreshData() {
        Snackbar.make(binding.root, "Refreshing credit data...", Snackbar.LENGTH_SHORT).show()
        viewModel.refreshCarbonCredits()
    }

    private fun applyFilters() {
        val selectedProject = binding.dropdownProjectFilter.text.toString()
        val selectedStatus = binding.dropdownStatusFilter.text.toString()
        viewModel.filterCredits(selectedProject, selectedStatus)
    }

    private fun updateSummaryCards(credits: List<CarbonCredit>) {
        val totalCredits = credits.sumOf { it.quantity }
        val availableCredits = credits.filter { it.status == CreditStatus.AVAILABLE || it.status == CreditStatus.ISSUED }.sumOf { it.quantity }
        val retiredCredits = credits.filter { it.status == CreditStatus.RETIRED }.sumOf { it.quantity }
        val totalRevenue = credits.sumOf { it.totalValue }

        binding.apply {
            textViewTotalCredits.text = String.format("%.1f", totalCredits)
            textViewAvailableCredits.text = String.format("%.1f", availableCredits)
            textViewRetiredCredits.text = String.format("%.1f", retiredCredits)
            textViewRevenue.text = String.format("$%.0f", totalRevenue)
        }
    }

    private fun showCreditDetails(credit: CarbonCredit) {
        Snackbar.make(binding.root, "Viewing details for ${credit.batchId}", Snackbar.LENGTH_SHORT).show()
    }

    private fun requestVerification() {
        Snackbar.make(binding.root, "Verification request submitted", Snackbar.LENGTH_SHORT).show()
    }

    private fun exportReport() {
        Snackbar.make(binding.root, "Exporting credit report...", Snackbar.LENGTH_SHORT).show()
    }

    private fun viewMarketplace() {
        Snackbar.make(binding.root, "Opening carbon marketplace...", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
