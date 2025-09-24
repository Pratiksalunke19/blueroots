package com.blueroots.carbonregistry.ui.registration

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.data.blockchain.HederaTransactionResult
import com.blueroots.carbonregistry.data.models.*
import com.blueroots.carbonregistry.databinding.FragmentProjectRegistrationBinding
import com.blueroots.carbonregistry.viewmodel.ProjectRegistrationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.blueroots.carbonregistry.viewmodel.CreditViewModel
import com.blueroots.carbonregistry.data.models.EcosystemType

class ProjectRegistrationFragment : Fragment() {

    private var _binding: FragmentProjectRegistrationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProjectRegistrationViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var selectedStartDate: Calendar = Calendar.getInstance()

    // Location permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }
            else -> {
                Snackbar.make(binding.root, "Location permission denied", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupDropdowns()
        setupClickListeners()
        observeViewModel()

        viewModel.registrationStatus.observe(viewLifecycleOwner) { status ->
            // Show status in a Snackbar or Dialog
            Snackbar.make(binding.root, status, Snackbar.LENGTH_LONG).show()
        }
        viewModel.blockchainRegistration.observe(viewLifecycleOwner) { result ->
            result?.let {
                // Show transaction details dialog
                showTransactionDialog(it)
            }
        }
    }

    private fun showTransactionDialog(result: HederaTransactionResult) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Blockchain Transaction Successful")
            .setMessage("""
            Transaction ID: ${result.transactionId}
            Consensus Time: ${result.consensusTimestamp}
            Status: ${result.status}
            Network Fee: ${result.fee}
        """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupDropdowns() {
        // Ecosystem Type Dropdown
        val ecosystemTypes = listOf("Mangrove", "Seagrass", "Salt Marsh", "Coastal Wetland")
        val ecosystemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ecosystemTypes)
        binding.dropdownEcosystemType.setAdapter(ecosystemAdapter)

        // Funding Source Dropdown
        val fundingSources = listOf(
            "Private Investment",
            "Public Funding",
            "Blended Finance",
            "Development Finance",
            "Carbon Finance",
            "Community-Based Funding"
        )
        val fundingAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, fundingSources)
        binding.dropdownFundingSource.setAdapter(fundingAdapter)

        // Methodology Dropdown
        val methodologies = listOf(
            "VCS VM0007 - Restoration of degraded coastal wetlands",
            "CDM AMS-III.BF - Small-scale wetland restoration",
            "Gold Standard Wetland Restoration",
            "Blue Carbon Accelerator Method",
            "Custom Methodology"
        )
        val methodologyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, methodologies)
        binding.dropdownMethodology.setAdapter(methodologyAdapter)
    }

    private fun setupClickListeners() {
        binding.apply {
            // Get Location Button
            buttonGetLocation.setOnClickListener {
                checkLocationPermissionAndGetLocation()
            }

            // Start Date Picker
            editTextStartDate.setOnClickListener {
                showDatePicker()
            }

            // Submit Button
            buttonSubmitProject.setOnClickListener {
                if (validateForm()) {
                    submitProject()
                }
            }
        }
    }

    private fun checkLocationPermissionAndGetLocation() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            binding.buttonGetLocation.isEnabled = false
            binding.buttonGetLocation.text = "Getting Location..."

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    binding.buttonGetLocation.isEnabled = true
                    binding.buttonGetLocation.text = "Get Current Location"

                    location?.let {
                        binding.editTextLatitude.setText(String.format("%.6f", it.latitude))
                        binding.editTextLongitude.setText(String.format("%.6f", it.longitude))
                        Snackbar.make(binding.root, "Location captured successfully", Snackbar.LENGTH_SHORT).show()
                    } ?: run {
                        Snackbar.make(binding.root, "Unable to get current location", Snackbar.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    binding.buttonGetLocation.isEnabled = true
                    binding.buttonGetLocation.text = "Get Current Location"
                    Snackbar.make(binding.root, "Failed to get location: ${it.message}", Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedStartDate.set(Calendar.YEAR, year)
                selectedStartDate.set(Calendar.MONTH, month)
                selectedStartDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                binding.editTextStartDate.setText(dateFormat.format(selectedStartDate.time))
            },
            selectedStartDate.get(Calendar.YEAR),
            selectedStartDate.get(Calendar.MONTH),
            selectedStartDate.get(Calendar.DAY_OF_MONTH)
        )

        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun validateForm(): Boolean {
        var isValid = true

        binding.apply {
            // Validate required fields
            if (editTextProjectName.text.toString().trim().isEmpty()) {
                editTextProjectName.error = "Project name is required"
                isValid = false
            }

            if (editTextProjectDescription.text.toString().trim().isEmpty()) {
                editTextProjectDescription.error = "Project description is required"
                isValid = false
            }

            if (editTextProjectArea.text.toString().trim().isEmpty()) {
                editTextProjectArea.error = "Project area is required"
                isValid = false
            }

            if (editTextLatitude.text.toString().trim().isEmpty()) {
                editTextLatitude.error = "Latitude is required"
                isValid = false
            }

            if (editTextLongitude.text.toString().trim().isEmpty()) {
                editTextLongitude.error = "Longitude is required"
                isValid = false
            }

            if (editTextCountry.text.toString().trim().isEmpty()) {
                editTextCountry.error = "Country is required"
                isValid = false
            }

            if (editTextState.text.toString().trim().isEmpty()) {
                editTextState.error = "State/Province is required"
                isValid = false
            }

            if (editTextOrganization.text.toString().trim().isEmpty()) {
                editTextOrganization.error = "Organization name is required"
                isValid = false
            }

            if (editTextContactPerson.text.toString().trim().isEmpty()) {
                editTextContactPerson.error = "Contact person is required"
                isValid = false
            }

            if (editTextContactEmail.text.toString().trim().isEmpty()) {
                editTextContactEmail.error = "Email is required"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(editTextContactEmail.text.toString()).matches()) {
                editTextContactEmail.error = "Please enter a valid email"
                isValid = false
            }

            // Validate numeric fields
            try {
                val latitude = editTextLatitude.text.toString().toDoubleOrNull()
                if (latitude == null || latitude < -90 || latitude > 90) {
                    editTextLatitude.error = "Please enter a valid latitude (-90 to 90)"
                    isValid = false
                }
            } catch (e: Exception) {
                editTextLatitude.error = "Please enter a valid latitude"
                isValid = false
            }

            try {
                val longitude = editTextLongitude.text.toString().toDoubleOrNull()
                if (longitude == null || longitude < -180 || longitude > 180) {
                    editTextLongitude.error = "Please enter a valid longitude (-180 to 180)"
                    isValid = false
                }
            } catch (e: Exception) {
                editTextLongitude.error = "Please enter a valid longitude"
                isValid = false
            }
        }

        return isValid
    }

    private fun submitProject() {
        val projectRegistration = createProjectFromForm()

        // Convert ProjectRegistration to Map<String, Any>
        val projectData = projectRegistrationToMap(projectRegistration)

        // Submit to viewModel (if you have a submitProject method that takes Map)
        // viewModel.submitProject(projectData)

        // Trigger credit issuance demo
        triggerCreditIssuanceDemo(projectRegistration)
    }

    private fun projectRegistrationToMap(project: ProjectRegistration): Map<String, Any> {
        return mapOf(
            "id" to (project.id ?: UUID.randomUUID().toString()),
            "name" to (project.projectName ?: ""),
            "description" to (project.projectDescription ?: ""),
            // Combine location fields into a single location string
            "location" to "${project.nearestCity}, ${project.district}, ${project.state}, ${project.country}".trim().removeSuffix(","),
            "latitude" to project.latitude,
            "longitude" to project.longitude,
            "country" to project.country,
            "state" to project.state,
            "district" to project.district,
            "nearestCity" to project.nearestCity,
            "area" to project.projectArea, // Use projectArea instead of area
            "ecosystemType" to (project.projectType.name ?: "MANGROVE"),
            "startDate" to (project.startDate?.toString() ?: ""),
            "endDate" to (project.endDate?.toString() ?: ""),
            "methodology" to (project.methodology ?: ""),
            "expectedCredits" to (project.expectedCreditGeneration ?: 0.0)
        )
    }

    private fun triggerCreditIssuanceDemo(project: ProjectRegistration) {
        val creditViewModel = ViewModelProvider(requireActivity())[CreditViewModel::class.java]

        // Create a proper location string
        val locationString = listOf(
            project.nearestCity,
            project.district,
            project.state,
            project.country
        ).filter { it.isNotBlank() }.joinToString(", ")

        creditViewModel.processProjectReviewAndIssueCredits(
            projectId = project.id ?: UUID.randomUUID().toString(),
            projectName = project.projectName ?: "Unnamed Project",
            area = project.projectArea, // Use projectArea
            ecosystemType = project.projectType ?: EcosystemType.MANGROVE,
            location = locationString.ifBlank { "Unknown Location" }
        )

        Snackbar.make(binding.root, "Project submitted to Hedera blockchain!", Snackbar.LENGTH_LONG)
            .setAction("View Credits") {
                // Use the correct navigation ID
                findNavController().navigate(R.id.creditIssuanceFragment)
            }.show()
    }


    private fun createProjectFromForm(): ProjectRegistration {
        binding.apply {
            // Get selected standards
            val selectedStandards = mutableListOf<String>()
            if (chipVCS.isChecked) selectedStandards.add("VCS")
            if (chipGoldStandard.isChecked) selectedStandards.add("Gold Standard")
            if (chipCDM.isChecked) selectedStandards.add("CDM")
            if (chipCAR.isChecked) selectedStandards.add("CAR")

            // Get selected safeguards
            val selectedSafeguards = mutableListOf<String>()
            if (chipFPIC.isChecked) selectedSafeguards.add("FPIC Compliance")
            if (chipGender.isChecked) selectedSafeguards.add("Gender Inclusive")
            if (chipCommunityBenefit.isChecked) selectedSafeguards.add("Community Benefit Sharing")

            // Map ecosystem type
            val ecosystemType = when (dropdownEcosystemType.text.toString()) {
                "Mangrove" -> EcosystemType.MANGROVE
                "Seagrass" -> EcosystemType.SEAGRASS
                "Salt Marsh" -> EcosystemType.SALT_MARSH
                "Coastal Wetland" -> EcosystemType.COASTAL_WETLAND
                else -> EcosystemType.MANGROVE
            }

            // Map funding source
            val fundingSource = when (dropdownFundingSource.text.toString()) {
                "Private Investment" -> FundingSource.PRIVATE
                "Public Funding" -> FundingSource.PUBLIC
                "Blended Finance" -> FundingSource.BLENDED
                "Development Finance" -> FundingSource.DEVELOPMENT_FINANCE
                "Carbon Finance" -> FundingSource.CARBON_FINANCE
                "Community-Based Funding" -> FundingSource.COMMUNITY_BASED
                else -> FundingSource.PRIVATE
            }

            // Map methodology
            val methodology = when {
                dropdownMethodology.text.toString().contains("VM0007") -> CarbonMethodology.VCS_VM0007
                dropdownMethodology.text.toString().contains("AMS-III.BF") -> CarbonMethodology.CDM_AMS_III_BF
                dropdownMethodology.text.toString().contains("Gold Standard") -> CarbonMethodology.GOLD_STANDARD_WETLAND
                dropdownMethodology.text.toString().contains("Blue Carbon") -> CarbonMethodology.BLUE_CARBON_ACCELERATOR
                else -> CarbonMethodology.CUSTOM
            }

            val projectDeveloper = ProjectDeveloper(
                organizationName = editTextOrganization.text.toString().trim(),
                contactPerson = editTextContactPerson.text.toString().trim(),
                email = editTextContactEmail.text.toString().trim(),
                organizationType = OrganizationType.PRIVATE_COMPANY
            )

            return ProjectRegistration(
                id = UUID.randomUUID().toString(),
                projectName = editTextProjectName.text.toString().trim(),
                projectDescription = editTextProjectDescription.text.toString().trim(),
                projectType = ecosystemType,
                latitude = editTextLatitude.text.toString().toDoubleOrNull() ?: 0.0,
                longitude = editTextLongitude.text.toString().toDoubleOrNull() ?: 0.0,
                country = editTextCountry.text.toString().trim(),
                state = editTextState.text.toString().trim(),
                district = editTextDistrict.text.toString().trim(),
                nearestCity = editTextNearestCity.text.toString().trim(),
                projectArea = editTextProjectArea.text.toString().toDoubleOrNull() ?: 0.0,
                startDate = selectedStartDate.time,
                projectDuration = editTextDuration.text.toString().toIntOrNull() ?: 0,
                credatingPeriod = editTextCreditingPeriod.text.toString().toIntOrNull() ?: 0,
                estimatedInvestment = editTextInvestment.text.toString().toDoubleOrNull() ?: 0.0,
                fundingSource = fundingSource,
                expectedCreditGeneration = editTextExpectedCredits.text.toString().toDoubleOrNull() ?: 0.0,
                projectDeveloper = projectDeveloper,
                localCommunityPartner = editTextCommunityPartner.text.toString().trim(),
                existingVegetation = editTextVegetation.text.toString().trim(),
                soilType = editTextSoilType.text.toString().trim(),
                hydrologyDetails = editTextHydrology.text.toString().trim(),
                methodology = methodology,
                standardsCompliance = selectedStandards,
                socialSafeguards = selectedSafeguards,
                projectStatus = ProjectStatus.PLANNING,
                createdAt = Date(),
                updatedAt = Date()
            )
        }
    }

    private fun observeViewModel() {
        viewModel.submissionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ProjectRegistrationViewModel.SubmissionResult.Success -> {
                    Snackbar.make(binding.root, "Project submitted successfully!", Snackbar.LENGTH_LONG).show()
                    clearForm()
                }
                is ProjectRegistrationViewModel.SubmissionResult.Error -> {
                    Snackbar.make(binding.root, "Error: ${result.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.buttonSubmitProject.isEnabled = !isLoading
            binding.buttonSubmitProject.text = if (isLoading) "Submitting..." else "Submit Project for Review"
        }
    }

    private fun clearForm() {
        binding.apply {
            editTextProjectName.text?.clear()
            editTextProjectDescription.text?.clear()
            editTextProjectArea.text?.clear()
            editTextLatitude.text?.clear()
            editTextLongitude.text?.clear()
            editTextCountry.text?.clear()
            editTextState.text?.clear()
            editTextDistrict.text?.clear()
            editTextNearestCity.text?.clear()
            editTextStartDate.text?.clear()
            editTextDuration.text?.clear()
            editTextCreditingPeriod.text?.clear()
            editTextInvestment.text?.clear()
            editTextExpectedCredits.text?.clear()
            editTextOrganization.text?.clear()
            editTextContactPerson.text?.clear()
            editTextContactEmail.text?.clear()
            editTextCommunityPartner.text?.clear()
            editTextVegetation.text?.clear()
            editTextSoilType.text?.clear()
            editTextHydrology.text?.clear()

            // Clear chips
            chipGroupStandards.clearCheck()
            chipGroupSafeguards.clearCheck()

            // Reset dropdowns
            dropdownEcosystemType.setText("Mangrove", false)
            dropdownFundingSource.setText("", false)
            dropdownMethodology.setText("VCS VM0007 - Restoration of degraded coastal wetlands", false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
