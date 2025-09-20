package com.blueroots.carbonregistry.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blueroots.carbonregistry.databinding.FragmentSignupBinding
import com.blueroots.carbonregistry.viewmodel.AuthViewModel
import com.blueroots.carbonregistry.viewmodel.AuthStatus

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.buttonSignUp.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            val confirmPassword = binding.editConfirmPassword.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                authViewModel.signUp(name, email, password)
            }
        }

        binding.textSignIn.setOnClickListener {
            // Navigate back to LoginFragment
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeViewModel() {
        authViewModel.signUpStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is AuthStatus.Loading -> {
                    binding.buttonSignUp.isEnabled = false
                    binding.progressSignUp.visibility = View.VISIBLE
                }
                is AuthStatus.Success -> {
                    binding.buttonSignUp.isEnabled = true
                    binding.progressSignUp.visibility = View.GONE
                    Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                    // Navigate back to login
                    parentFragmentManager.popBackStack()
                }
                is AuthStatus.Error -> {
                    binding.buttonSignUp.isEnabled = true
                    binding.progressSignUp.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${status.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            binding.editName.error = "Full name is required"
            return false
        }
        if (email.isEmpty()) {
            binding.editEmail.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.error = "Please enter a valid email"
            return false
        }
        if (password.isEmpty()) {
            binding.editPassword.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            binding.editPassword.error = "Password must be at least 6 characters"
            return false
        }
        if (confirmPassword.isEmpty()) {
            binding.editConfirmPassword.error = "Please confirm your password"
            return false
        }
        if (password != confirmPassword) {
            binding.editConfirmPassword.error = "Passwords do not match"
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
