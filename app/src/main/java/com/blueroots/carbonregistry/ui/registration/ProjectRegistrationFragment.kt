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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.data.blockchain.HederaTransactionResult
import com.blueroots.carbonregistry.data.models.*
import com.blueroots.carbonregistry.databinding.FragmentProjectRegistrationBinding
import com.blueroots.carbonregistry.viewmodel.ProjectRegistrationViewModel
import com.blueroots.carbonregistry.viewmodel.CreditViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class ProjectRegistrationFragment : Fragment() {

    private var _binding: FragmentProjectRegistrationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProjectRegistrationViewModel by viewModels()
    private val creditViewModel: CreditViewModel by activityViewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var selectedStartDate: Calendar = Calendar.getInstance()

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

        // 🔹 Auto-fill demo data for testing
        fillDemoData()
    }

    private fun setupDropdowns() {
        val ecosystemTypes = listOf("Mangrove", "Seagrass", "Salt Marsh", "Coastal Wetland")
        val ecosystemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ecosystemTypes)
        binding.dropdownEcosystemType.setAdapter(ecosystemAdapter)

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

    private fun fillDemoData() {
        binding.apply {
            editTextProjectName.setText("BlueRoots Mangrove Restoration Pilot")
            editTextProjectDescription.setText("A demonstration project for restoring mangrove ecosystems in Goa, improving carbon sequestration and biodiversity.")
            dropdownEcosystemType.setText("Mangrove", false)
            dropdownFundingSource.setText("Carbon Finance", false)
            dropdownMethodology.setText("VCS VM0007 - Restoration of degraded coastal wetlands", false)

            editTextLatitude.setText("15.2993")
            editTextLongitude.setText("74.1240")
            editTextCountry.setText("India")
            editTextState.setText("Goa")
            editTextDistrict.setText("North Goa")
            editTextNearestCity.setText("Panaji")
            editTextProjectArea.setText("25.0")
            editTextDuration.setText("10")
            editTextCreditingPeriod.setText("5")
            editTextInvestment.setText("1200000")
            editTextExpectedCredits.setText("50000")

            editTextOrganization.setText("BlueRoots Research Foundation")
            editTextContactPerson.setText("Dr. Aisha Fernandes")
            editTextContactEmail.setText("demo@blueroots.org")
            editTextCommunityPartner.setText("Goa Coastal Community Group")
            editTextVegetation.setText("Dense mangrove and salt-tolerant flora")
            editTextSoilType.setText("Silty clay with moderate salinity")
            editTextHydrology.setText("Tidal estuarine hydrology")

            selectedStartDate.add(Calendar.DAY_OF_MONTH, 2)
            editTextStartDate.setText(dateFormat.format(selectedStartDate.time))

            chipVCS.isChecked = true
            chipFPIC.isChecked = true
            chipCommunityBenefit.isChecked = true
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            buttonGetLocation.setOnClickListener {
                checkLocationPermissionAndGetLocation()
            }

            editTextStartDate.setOnClickListener {
                showDatePicker()
            }

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
                        Snackbar.make(binding.root, "📍 Location captured successfully", Snackbar.LENGTH_SHORT).show()
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

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun validateForm(): Boolean {
        var isValid = true
        binding.apply {
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
        }
        return isValid
    }

    private fun submitProject() {
        val projectRegistration = createProjectFromForm()
        Snackbar.make(binding.root, "Registering project...", Snackbar.LENGTH_LONG).show()
        binding.buttonSubmitProject.isEnabled = false
        binding.buttonSubmitProject.text = "Registering Project..."
        viewModel.saveProjectWithSync(projectRegistration)
        triggerCreditIssuanceDemo(projectRegistration)
    }

    private fun triggerCreditIssuanceDemo(project: ProjectRegistration) {
        val locationString = listOf(
            project.nearestCity,
            project.district,
            project.state,
            project.country
        ).filter { it.isNotBlank() }.joinToString(", ")

        creditViewModel.processProjectReviewAndIssueCredits(
            projectId = project.id ?: UUID.randomUUID().toString(),
            projectName = project.projectName ?: "Unnamed Project",
            area = project.projectArea,
            ecosystemType = project.projectType,
            location = locationString.ifBlank { "Unknown Location" }
        )
    }

    private fun createProjectFromForm(): ProjectRegistration {
        binding.apply {
            val selectedStandards = mutableListOf<String>()
            if (chipVCS.isChecked) selectedStandards.add("VCS")
            if (chipGoldStandard.isChecked) selectedStandards.add("Gold Standard")
            if (chipCDM.isChecked) selectedStandards.add("CDM")
            if (chipCAR.isChecked) selectedStandards.add("CAR")

            val selectedSafeguards = mutableListOf<String>()
            if (chipFPIC.isChecked) selectedSafeguards.add("FPIC Compliance")
            if (chipGender.isChecked) selectedSafeguards.add("Gender Inclusive")
            if (chipCommunityBenefit.isChecked) selectedSafeguards.add("Community Benefit Sharing")

            val ecosystemType = when (dropdownEcosystemType.text.toString()) {
                "Mangrove" -> EcosystemType.MANGROVE
                "Seagrass" -> EcosystemType.SEAGRASS
                "Salt Marsh" -> EcosystemType.SALT_MARSH
                "Coastal Wetland" -> EcosystemType.COASTAL_WETLAND
                else -> EcosystemType.MANGROVE
            }

            val fundingSource = when (dropdownFundingSource.text.toString()) {
                "Private Investment" -> FundingSource.PRIVATE
                "Public Funding" -> FundingSource.PUBLIC
                "Blended Finance" -> FundingSource.BLENDED
                "Development Finance" -> FundingSource.DEVELOPMENT_FINANCE
                "Carbon Finance" -> FundingSource.CARBON_FINANCE
                "Community-Based Funding" -> FundingSource.COMMUNITY_BASED
                else -> FundingSource.PRIVATE
            }

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
                    Snackbar.make(binding.root, "✅ ${result.message}", Snackbar.LENGTH_LONG).show()
                    clearForm()
                    binding.buttonSubmitProject.isEnabled = true
                    binding.buttonSubmitProject.text = "Submit Project for Review"
                }
                is ProjectRegistrationViewModel.SubmissionResult.Error -> {
                    Snackbar.make(binding.root, "❌ Error: ${result.message}", Snackbar.LENGTH_LONG).show()
                    binding.buttonSubmitProject.isEnabled = true
                    binding.buttonSubmitProject.text = "Submit Project for Review"
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!isLoading) {
                binding.buttonSubmitProject.isEnabled = true
                binding.buttonSubmitProject.text = "Submit Project for Review"
            }
        }

        viewModel.syncStatus.observe(viewLifecycleOwner) { status ->
            if (status.isNotEmpty()) {
                Snackbar.make(binding.root, status, Snackbar.LENGTH_SHORT).show()
            }
        }

        creditViewModel.blockchainStatus.observe(viewLifecycleOwner) { status ->
            if (status.isNotEmpty()) {
                Snackbar.make(binding.root, status, Snackbar.LENGTH_SHORT).show()
            }
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

            chipGroupStandards.clearCheck()
            chipGroupSafeguards.clearCheck()

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
