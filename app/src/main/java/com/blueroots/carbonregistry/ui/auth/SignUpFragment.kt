package com.blueroots.carbonregistry.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog  // FIXED: Import the correct AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.databinding.FragmentSignupBinding
import com.blueroots.carbonregistry.viewmodel.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    // FIXED: Use androidx.appcompat.app.AlertDialog instead of android.app.AlertDialog
    private var successDialog: AlertDialog? = null

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
                    // Show success dialog
                    showSuccessDialog()
                    // Clear the result to prevent showing dialog again
                    authViewModel.clearSignUpResult()
                }
                is AuthViewModel.AuthResult.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    // Also clear error result
                    authViewModel.clearSignUpResult()
                }
            }
        }
    }

    private fun showSuccessDialog() {
        // Dismiss any existing dialog first
        successDialog?.dismiss()

        successDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Account Created Successfully!")
            .setMessage("Please check your email and click the verification link before signing in.")
            .setIcon(R.drawable.ic_email_24)
            .setPositiveButton("Go to Login") { dialog, _ ->
                dialog.dismiss()
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .create()

        successDialog?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Dismiss dialog when fragment is destroyed
        successDialog?.dismiss()
        successDialog = null
        _binding = null
    }
}
