package com.blueroots.carbonregistry.ui.monitoring

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.data.models.*
import com.blueroots.carbonregistry.databinding.FragmentMonitoringUploadBinding
import com.blueroots.carbonregistry.ui.adapters.PhotoAdapter
import com.blueroots.carbonregistry.viewmodel.MonitoringViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class MonitoringUploadFragment : Fragment() {

    private var _binding: FragmentMonitoringUploadBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MonitoringViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var photoAdapter: PhotoAdapter
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var selectedMonitoringDate: Calendar = Calendar.getInstance()
    private val selectedPhotos = mutableListOf<MonitoringPhoto>()

    private val PREFS_NAME = "monitoring_upload_notifications"

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) addPhotoToCollection("camera_photo_${System.currentTimeMillis()}.jpg")
    }

    private val selectPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { addPhotoToCollection(it.toString()) }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> getCurrentLocation()
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> getCurrentLocation()
            else -> Snackbar.make(binding.root, "Location permission denied", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitoringUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        clearOldNotifications()

        setupRecyclerView()
        setupDropdowns()
        setupClickListeners()
        observeViewModel()
        updateUIBasedOnDataType()

        // 🔹 Prefill demo data for testing
        prefillDemoData()
    }

    private fun prefillDemoData() {
        binding.apply {
            dropdownProject.setText("Sundarbans Mangrove Restoration", false)
            dropdownDataType.setText("Soil Sample", false)
            editTextMonitoringDate.setText("05/10/2025")
            editTextDataCollector.setText("Dr. Riya Menon")
            editTextQualifications.setText("Field Ecologist, PhD")
            dropdownPriority.setText("Medium", false)
            editTextLatitude.setText("21.945123")
            editTextLongitude.setText("88.732451")
            editTextAltitude.setText("7.4")
            editTextPlotId.setText("Plot-A12")
            editTextTransectId.setText("Transect-3")
            editTextSiteDescription.setText("Coastal mangrove site near delta edge")
            editTextSampleId.setText("SS-1024")
            editTextSampleDepth.setText("15.5")
            editTextOrganicCarbon.setText("3.2")
            editTextBulkDensity.setText("1.3")
            editTextMoisture.setText("28.7")
            editTextPH.setText("7.1")
            editTextSalinity.setText("0.4")
            editTextSoilTemperature.setText("27.6")
            editTextEquipment.setText("Soil auger, GPS, Moisture probe")
            dropdownComplianceStandard.setText("VCS", false)
            editTextMethodologyVersion.setText("VCS 4.2.1")
            editTextReportingPeriod.setText("Q3 2025")
            editTextNotes.setText("All sampling conducted post-tide; conditions stable.")

            // Mark data complete
            chipDataComplete.isChecked = true

            // Add 2 demo photos
            addPhotoToCollection("sample_soil_photo_1.jpg")
            addPhotoToCollection("sample_site_photo_2.jpg")

            updateStatus()
        }
    }

    private fun setupRecyclerView() {
        photoAdapter = PhotoAdapter(selectedPhotos) { photo ->
            removePhoto(photo)
        }
        binding.recyclerViewPhotos.apply {
            adapter = photoAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupDropdowns() {
        // Project Dropdown
        val projects = listOf(
            "Sundarbans Mangrove Restoration",
            "Gulf Coast Blue Carbon Project",
            "Pacific Seagrass Conservation",
            "Coastal Wetland Recovery"
        )
        val projectAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, projects)
        binding.dropdownProject.setAdapter(projectAdapter)

        // Data Type Dropdown
        val dataTypes = MonitoringDataType.values().map { it.displayName }
        val dataTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, dataTypes)
        binding.dropdownDataType.setAdapter(dataTypeAdapter)

        // Priority Dropdown
        val priorities = listOf("Low", "Medium", "High", "Urgent")
        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, priorities)
        binding.dropdownPriority.setAdapter(priorityAdapter)

        // Sampling Method Dropdown
        val samplingMethods = listOf("Core Sampling", "Auger Sampling", "Pit Sampling", "Composite Sampling")
        val samplingAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, samplingMethods)
        binding.dropdownSamplingMethod.setAdapter(samplingAdapter)

        // Compliance Standard Dropdown
        val standards = listOf("VCS", "Gold Standard", "CDM", "CAR", "Plan Vivo")
        val standardsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, standards)
        binding.dropdownComplianceStandard.setAdapter(standardsAdapter)

        // Setup dropdown change listener
        binding.dropdownDataType.setOnItemClickListener { _, _, _, _ ->
            updateUIBasedOnDataType()
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Location button
            buttonGetCurrentLocation.setOnClickListener {
                checkLocationPermissionAndGetLocation()
            }

            // Date picker
            editTextMonitoringDate.setOnClickListener {
                showDatePicker()
            }

            // Photo buttons
            buttonTakePhoto.setOnClickListener {
                takePhoto()
            }

            buttonSelectPhoto.setOnClickListener {
                selectPhotoFromGallery()
            }

            // Upload button - UPDATED to use new sync method
            buttonUploadData.setOnClickListener {
                if (validateForm()) {
                    uploadMonitoringData()
                }
            }
        }
    }

    private fun updateUIBasedOnDataType() {
        val selectedType = binding.dropdownDataType.text.toString()

        // Show/hide sections based on data type
        binding.cardSoilData.visibility = if (selectedType == "Soil Sample") {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Update status and requirements based on data type
        updateStatus()
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
            binding.buttonGetCurrentLocation.isEnabled = false
            binding.buttonGetCurrentLocation.text = "Getting Location..."

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    binding.buttonGetCurrentLocation.isEnabled = true
                    binding.buttonGetCurrentLocation.text = "Get Current Location"

                    location?.let {
                        binding.editTextLatitude.setText(String.format("%.6f", it.latitude))
                        binding.editTextLongitude.setText(String.format("%.6f", it.longitude))
                        binding.editTextAltitude.setText(String.format("%.1f", it.altitude))
                        Snackbar.make(binding.root, "📍 Location captured successfully", Snackbar.LENGTH_SHORT).show()
                        updateStatus()
                    } ?: run {
                        Snackbar.make(binding.root, "Unable to get current location", Snackbar.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    binding.buttonGetCurrentLocation.isEnabled = true
                    binding.buttonGetCurrentLocation.text = "Get Current Location"
                    Snackbar.make(binding.root, "Failed to get location: ${it.message}", Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedMonitoringDate.set(Calendar.YEAR, year)
                selectedMonitoringDate.set(Calendar.MONTH, month)
                selectedMonitoringDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                binding.editTextMonitoringDate.setText(dateFormat.format(selectedMonitoringDate.time))
                updateStatus()
            },
            selectedMonitoringDate.get(Calendar.YEAR),
            selectedMonitoringDate.get(Calendar.MONTH),
            selectedMonitoringDate.get(Calendar.DAY_OF_MONTH)
        )

        // Set maximum date to today
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun takePhoto() {
        // For simplicity, we'll simulate photo capture
        // In a real implementation, you'd use camera intent
        addPhotoToCollection("captured_${System.currentTimeMillis()}.jpg")
    }

    private fun selectPhotoFromGallery() {
        selectPhotoLauncher.launch("image/*")
    }

    private fun addPhotoToCollection(filename: String) {
        val photo = MonitoringPhoto(
            id = UUID.randomUUID().toString(),
            filename = filename,
            localPath = filename,
            caption = "",
            gpsCoordinates = "${binding.editTextLatitude.text}, ${binding.editTextLongitude.text}",
            timestamp = Date(),
            photoType = PhotoType.GENERAL
        )

        selectedPhotos.add(photo)
        photoAdapter.notifyItemInserted(selectedPhotos.size - 1)

        if (selectedPhotos.isNotEmpty()) {
            binding.recyclerViewPhotos.visibility = View.VISIBLE
        }

        updateStatus()
    }

    private fun removePhoto(photo: MonitoringPhoto) {
        val index = selectedPhotos.indexOf(photo)
        if (index != -1) {
            selectedPhotos.removeAt(index)
            photoAdapter.notifyItemRemoved(index)

            if (selectedPhotos.isEmpty()) {
                binding.recyclerViewPhotos.visibility = View.GONE
            }

            updateStatus()
        }
    }

    private fun updateStatus() {
        val isFormComplete = isFormComplete()
        val qualityChecksComplete = areQualityChecksComplete()

        binding.apply {
            when {
                isFormComplete && qualityChecksComplete -> {
                    textViewStatus.text = "Status: ✅ Ready for Upload"
                    textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_ready))
                    buttonUploadData.isEnabled = true
                }
                isFormComplete -> {
                    textViewStatus.text = "Status: ⚠️ Complete Quality Checks"
                    textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_warning))
                    // Allow upload even without all quality checks completed
                    buttonUploadData.isEnabled = true
                }
                else -> {
                    textViewStatus.text = "Status: ❌ Incomplete Data"
                    textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_error))
                    buttonUploadData.isEnabled = false
                }
            }
        }
    }

    private fun isFormComplete(): Boolean {
        binding.apply {
            return dropdownProject.text.isNotEmpty() &&
                    editTextMonitoringDate.text!!.isNotEmpty() &&
                    editTextDataCollector.text!!.isNotEmpty() &&
                    editTextLatitude.text!!.isNotEmpty() &&
                    editTextLongitude.text!!.isNotEmpty()
        }
    }

    private fun areQualityChecksComplete(): Boolean {
        binding.apply {
            // Made quality checks more flexible
            // Only require data complete check, others are optional
            return chipDataComplete.isChecked
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        binding.apply {
            // Validate required fields
            if (dropdownProject.text.toString().trim().isEmpty()) {
                dropdownProject.error = "Please select a project"
                isValid = false
            }

            if (editTextMonitoringDate.text.toString().trim().isEmpty()) {
                editTextMonitoringDate.error = "Monitoring date is required"
                isValid = false
            }

            if (editTextDataCollector.text.toString().trim().isEmpty()) {
                editTextDataCollector.error = "Data collector name is required"
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

            // Validate coordinate ranges
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

    // UPDATED: Use new sync method
    private fun uploadMonitoringData() {
        val monitoringData = createMonitoringDataFromForm()

        // Disable button to prevent double submission
        binding.buttonUploadData.isEnabled = false
        binding.buttonUploadData.text = "Uploading..."

        // Use the new method with Supabase sync
        viewModel.uploadMonitoringDataWithSync(monitoringData)
    }

    private fun createMonitoringDataFromForm(): MonitoringData {
        binding.apply {
            // Map data type
            val dataType = MonitoringDataType.values().find {
                it.displayName == dropdownDataType.text.toString()
            } ?: MonitoringDataType.SOIL_SAMPLE

            // Map priority
            val priority = when (dropdownPriority.text.toString()) {
                "Low" -> Priority.LOW
                "Medium" -> Priority.MEDIUM
                "High" -> Priority.HIGH
                "Urgent" -> Priority.URGENT
                else -> Priority.MEDIUM
            }

            // Create location
            val location = MonitoringLocation(
                latitude = editTextLatitude.text.toString().toDoubleOrNull() ?: 0.0,
                longitude = editTextLongitude.text.toString().toDoubleOrNull() ?: 0.0,
                altitude = editTextAltitude.text.toString().toDoubleOrNull() ?: 0.0,
                plotId = editTextPlotId.text.toString().trim(),
                transectId = editTextTransectId.text.toString().trim(),
                siteDescription = editTextSiteDescription.text.toString().trim(),
                gpsAccuracy = 5.0 // Default accuracy
            )

            // Create soil data if applicable
            val soilData = if (dataType == MonitoringDataType.SOIL_SAMPLE) {
                SoilData(
                    sampleId = editTextSampleId.text.toString().trim(),
                    sampleDepth = editTextSampleDepth.text.toString().toDoubleOrNull() ?: 0.0,
                    organicCarbonContent = editTextOrganicCarbon.text.toString().toDoubleOrNull() ?: 0.0,
                    bulkDensity = editTextBulkDensity.text.toString().toDoubleOrNull() ?: 0.0,
                    moisture = editTextMoisture.text.toString().toDoubleOrNull() ?: 0.0,
                    pH = editTextPH.text.toString().toDoubleOrNull() ?: 0.0,
                    salinity = editTextSalinity.text.toString().toDoubleOrNull() ?: 0.0,
                    sampleTemperature = editTextSoilTemperature.text.toString().toDoubleOrNull() ?: 0.0
                )
            } else null

            // Create quality checks
            val qualityChecks = mutableListOf<QualityCheck>()
            if (chipDataComplete.isChecked) {
                qualityChecks.add(QualityCheck(
                    checkType = "Data Completeness",
                    checkResult = QualityResult.PASS,
                    checker = editTextDataCollector.text.toString().trim(),
                    checkDate = Date()
                ))
            }

            return MonitoringData(
                id = UUID.randomUUID().toString(),
                projectId = UUID.randomUUID().toString(), // Would be mapped from project selection
                projectName = dropdownProject.text.toString(),
                dataType = dataType,
                monitoringDate = selectedMonitoringDate.time,
                reportingPeriod = editTextReportingPeriod.text.toString().trim(),
                location = location,
                soilData = soilData,
                photos = selectedPhotos,
                notes = editTextNotes.text.toString().trim(),
                dataCollector = editTextDataCollector.text.toString().trim(),
                collectorQualifications = editTextQualifications.text.toString().trim(),
                equipmentUsed = listOf(editTextEquipment.text.toString().trim()),
                complianceStandard = dropdownComplianceStandard.text.toString(),
                methodologyVersion = editTextMethodologyVersion.text.toString().trim(),
                qualityControlChecks = qualityChecks,
                priority = priority,
                isComplete = areQualityChecksComplete(),
                createdAt = Date(),
                updatedAt = Date(),
                syncStatus = SyncStatus.LOCAL
            )
        }
    }

    private fun observeViewModel() {
        viewModel.uploadResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is MonitoringViewModel.UploadResult.Success -> {
                    onUploadSuccess(result.message)
                    viewModel.clearUploadResult()
                }
                is MonitoringViewModel.UploadResult.Error -> {
                    Snackbar.make(binding.root, "❌ Error: ${result.message}", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(resources.getColor(R.color.status_error, null))
                        .show()
                    // Re-enable button on error
                    binding.buttonUploadData.isEnabled = true
                    binding.buttonUploadData.text = "Upload Monitoring Data"
                    viewModel.clearUploadResult()
                }
            }
        }

        // NEW: Observe cloud sync status
        viewModel.cloudSyncStatus.observe(viewLifecycleOwner) { status ->
            if (status.isNotEmpty()) {
                // Show sync status briefly (optional)
                // You can show this in a separate status TextView if you have one
            }
        }
    }

    private fun onUploadSuccess(message: String) {
        // Create unique key for this upload success notification
        val timestamp = System.currentTimeMillis()
        val notificationKey = "upload_success_${timestamp}"

        // Only show notification if it hasn't been shown before
        if (!hasNotificationBeenShown(notificationKey)) {
            markNotificationAsShown(notificationKey)

            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                .setAction("🔗 View Impact") {
                    // Navigate to Credits tab to see monitoring data
                    findNavController().navigate(R.id.creditIssuanceFragment)
                }
                .setActionTextColor(resources.getColor(R.color.status_ready, null))
                .show()
        }

        clearForm()

        // Re-enable button after successful upload
        binding.buttonUploadData.isEnabled = true
        binding.buttonUploadData.text = "Upload Monitoring Data"
    }

    // Helper methods for notification tracking
    private fun hasNotificationBeenShown(key: String): Boolean {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(key, false)
    }

    private fun markNotificationAsShown(key: String) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, true).apply()
    }

    // Optional: Method to clear old notifications (call this periodically or on app start)
    private fun clearOldNotifications() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Clear notifications older than 24 hours
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        val allEntries = prefs.all

        allEntries.keys.forEach { key ->
            if (key.startsWith("upload_success_")) {
                val timestamp = key.substringAfterLast("_").toLongOrNull()
                if (timestamp != null && timestamp < oneDayAgo) {
                    editor.remove(key)
                }
            }
        }
        editor.apply()
    }

    private fun navigateToMonitoringList() {
        // Navigate to credits tab to show the impact
        findNavController().navigate(R.id.creditIssuanceFragment)
    }

    private fun showMonitoringDataPreview() {
        viewModel.filteredMonitoringData.value?.let { data ->
            val recentEntries = data.take(3)
            val previewText = if (recentEntries.isNotEmpty()) {
                "📊 Recent Monitoring Entries:\n\n" + recentEntries.joinToString("\n\n") { entry ->
                    "• ${entry.dataType.displayName}\n" +
                            "  Project: ${entry.projectName}\n" +
                            "  Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(entry.monitoringDate)}\n" +
                            "  Status: ${entry.verificationStatus.name}"
                } + "\n\n📈 Total Entries: ${data.size}"
            } else {
                "No monitoring data available yet."
            }

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("📊 Monitoring Data Overview")
                .setMessage(previewText)
                .setPositiveButton("OK", null)
                .setNeutralButton("📊 See Impact on Credits") { _, _ ->
                    Snackbar.make(binding.root, "🔗 Monitoring data impacts credit calculations!", Snackbar.LENGTH_LONG).show()
                }
                .show()
        }
    }

    private fun clearForm() {
        binding.apply {
            dropdownProject.setText("", false)
            editTextReportingPeriod.text?.clear()
            dropdownDataType.setText("Soil Sample", false)
            editTextMonitoringDate.text?.clear()
            editTextDataCollector.text?.clear()
            editTextQualifications.text?.clear()
            dropdownPriority.setText("Medium", false)
            editTextLatitude.text?.clear()
            editTextLongitude.text?.clear()
            editTextAltitude.text?.clear()
            editTextPlotId.text?.clear()
            editTextTransectId.text?.clear()
            editTextSiteDescription.text?.clear()
            editTextSampleId.text?.clear()
            editTextSampleDepth.text?.clear()
            editTextOrganicCarbon.text?.clear()
            editTextPH.text?.clear()
            editTextBulkDensity.text?.clear()
            editTextMoisture.text?.clear()
            editTextSalinity.text?.clear()
            editTextSoilTemperature.text?.clear()
            editTextEquipment.text?.clear()
            dropdownComplianceStandard.setText("", false)
            editTextMethodologyVersion.text?.clear()
            editTextNotes.text?.clear()

            // Clear chips
            chipGroupQualityChecks.clearCheck()

            // Clear photos
            selectedPhotos.clear()
            photoAdapter.notifyDataSetChanged()
            recyclerViewPhotos.visibility = View.GONE
        }

        selectedMonitoringDate = Calendar.getInstance()
        updateStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
