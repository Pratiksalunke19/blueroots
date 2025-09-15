package com.blueroots.carbonregistry.ui.registration

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.databinding.FragmentProjectRegistrationBinding
import com.blueroots.carbonregistry.viewmodel.ProjectRegistrationViewModel
import com.blueroots.carbonregistry.viewmodel.RegistrationStatus
import com.blueroots.carbonregistry.data.models.ProjectRegistration

class ProjectRegistrationFragment : Fragment() {
    private var _binding: FragmentProjectRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProjectRegistrationViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0

    companion object {
        private const val LOCATION_REQUEST_CODE = 1001
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

        viewModel = ViewModelProvider(this)[ProjectRegistrationViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup spinner for ecosystem types
        val ecosystemTypes = arrayOf("Mangrove", "Seagrass", "Salt Marsh", "Kelp Forest")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ecosystemTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEcosystem.adapter = adapter

        binding.buttonGetLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.buttonSubmitProject.setOnClickListener {
            submitProject()
        }
    }

    private fun observeViewModel() {
        viewModel.registrationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is RegistrationStatus.Loading -> {
                    binding.buttonSubmitProject.isEnabled = false
                    Toast.makeText(requireContext(), "Registering project...", Toast.LENGTH_SHORT).show()
                }
                is RegistrationStatus.Success -> {
                    binding.buttonSubmitProject.isEnabled = true
                    Toast.makeText(requireContext(), "Project registered successfully!", Toast.LENGTH_LONG).show()
                }
                is RegistrationStatus.Error -> {
                    binding.buttonSubmitProject.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${status.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLatitude = it.latitude
                currentLongitude = it.longitude
                binding.textLatitude.text = "Lat: ${String.format("%.6f", it.latitude)}"
                binding.textLongitude.text = "Lng: ${String.format("%.6f", it.longitude)}"
            } ?: run {
                Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitProject() {
        val projectName = binding.editProjectName.text.toString().trim()
        if (projectName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter project name", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentLatitude == 0.0 || currentLongitude == 0.0) {
            Toast.makeText(requireContext(), "Please get location first", Toast.LENGTH_SHORT).show()
            return
        }

        val projectData = ProjectRegistration(
            name = projectName,
            latitude = currentLatitude,
            longitude = currentLongitude,
            ecosystemType = binding.spinnerEcosystem.selectedItem.toString()
        )

        viewModel.registerProject(projectData)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
