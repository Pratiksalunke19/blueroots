package com.blueroots.carbonregistry.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.databinding.FragmentSignupBinding
import com.blueroots.carbonregistry.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupDropdowns() {
        // Organization type dropdown
        val organizationTypes = listOf(
            "Private Company",
            "NGO",
            "Government Agency",
            "Research Institution",
            "Community Organization",
            "International Organization"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, organizationTypes)
        // Assuming you have a dropdown for organization type in your layout
        // binding.dropdownOrganizationType.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.apply {
            buttonSignUp.setOnClickListener {
                val email = editTextEmail.text.toString().trim()
                val password = editTextPassword.text.toString().trim()
                val confirmPassword = editTextConfirmPassword.text.toString().trim()
                val fullName = editTextFullName.text.toString().trim()
                val organizationName = editTextOrganizationName.text.toString().trim()
                val organizationType = "Private Company" // Default or from dropdown

                authViewModel.signUp(email, password, confirmPassword, fullName, organizationName, organizationType)
            }

            textViewLogin.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun observeViewModel() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.apply {
                buttonSignUp.isEnabled = !isLoading
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

                if (isLoading) {
                    buttonSignUp.text = "Creating Account..."
                } else {
                    buttonSignUp.text = getString(R.string.sign_up)
                }
            }
        }

        authViewModel.signUpResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthViewModel.AuthResult.Success -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    // Don't navigate - user needs to verify email first
                    findNavController().navigateUp() // Go back to login
                }
                is AuthViewModel.AuthResult.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
