package com.blueroots.carbonregistry.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blueroots.carbonregistry.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // Mock user data
        binding.textUserName.text = "John Doe"
        binding.textUserEmail.text = "john.doe@blueroots.com"
        binding.textUserRole.text = "Project Developer"
        binding.textProjectCount.text = "3"
        binding.textCreditsIssued.text = "150.5"
        binding.textCreditsRetired.text = "45.2"

        binding.buttonEditProfile.setOnClickListener {
            // Handle edit profile
        }

        binding.buttonLogout.setOnClickListener {
            // Handle logout
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
