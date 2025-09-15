package com.blueroots.carbonregistry.ui.monitoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blueroots.carbonregistry.databinding.FragmentMonitoringUploadBinding
import com.blueroots.carbonregistry.viewmodel.MonitoringViewModel
import com.blueroots.carbonregistry.viewmodel.UploadStatus
import com.blueroots.carbonregistry.data.models.MonitoringData

class MonitoringUploadFragment : Fragment() {
    private var _binding: FragmentMonitoringUploadBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MonitoringViewModel

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

        viewModel = ViewModelProvider(this)[MonitoringViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup spinner for data types
        val dataTypes = arrayOf("Soil Sample", "Water Quality", "Satellite Image", "Biomass Measurement")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dataTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDataType.adapter = adapter

        binding.buttonTakePhoto.setOnClickListener {
            // Implement camera functionality later
            Toast.makeText(requireContext(), "Camera feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.buttonUploadData.setOnClickListener {
            uploadMonitoringData()
        }
    }

    private fun observeViewModel() {
        viewModel.uploadStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is UploadStatus.Loading -> {
                    binding.buttonUploadData.isEnabled = false
                    binding.textUploadStatus.text = "Status: Uploading..."
                }
                is UploadStatus.Success -> {
                    binding.buttonUploadData.isEnabled = true
                    binding.textUploadStatus.text = "Status: Uploaded successfully!"
                    Toast.makeText(requireContext(), "Data uploaded for verification", Toast.LENGTH_LONG).show()
                }
                is UploadStatus.Error -> {
                    binding.buttonUploadData.isEnabled = true
                    binding.textUploadStatus.text = "Status: Upload failed"
                    Toast.makeText(requireContext(), "Error: ${status.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun uploadMonitoringData() {
        val notes = binding.editNotes.text.toString().trim()
        if (notes.isEmpty()) {
            Toast.makeText(requireContext(), "Please add some notes", Toast.LENGTH_SHORT).show()
            return
        }

        val monitoringData = MonitoringData(
            projectId = "demo-project-id", // Replace with actual project ID
            dataType = binding.spinnerDataType.selectedItem.toString(),
            timestamp = System.currentTimeMillis(),
            notes = notes
        )

        viewModel.uploadMonitoringData(monitoringData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
