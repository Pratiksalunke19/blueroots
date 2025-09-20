package com.blueroots.carbonregistry.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blueroots.carbonregistry.databinding.FragmentLoginBinding
import com.blueroots.carbonregistry.viewmodel.AuthViewModel
import com.blueroots.carbonregistry.viewmodel.AuthStatus // ADD THIS IMPORT

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (validateInput(email, password)) {
                authViewModel.login(email, password)
            }
        }

        binding.textSignUp.setOnClickListener {
            // Navigate to SignUpFragment
            parentFragmentManager.beginTransaction()
                .replace(android.R.id.content, SignUpFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.textForgotPassword.setOnClickListener {
            // Handle forgot password
            Toast.makeText(requireContext(), "Forgot password coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        authViewModel.loginStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is AuthStatus.Loading -> {
                    binding.buttonLogin.isEnabled = false
                    binding.progressLogin.visibility = View.VISIBLE
                }
                is AuthStatus.Success -> {
                    binding.buttonLogin.isEnabled = true
                    binding.progressLogin.visibility = View.GONE
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to MainActivity
                }
                is AuthStatus.Error -> {
                    binding.buttonLogin.isEnabled = true
                    binding.progressLogin.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${status.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.editEmail.error = "Email is required"
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
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
