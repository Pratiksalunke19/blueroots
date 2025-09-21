package com.blueroots.carbonregistry.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.databinding.FragmentLoginBinding
import com.blueroots.carbonregistry.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()

        // Check if user is already logged in
        authViewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn && findNavController().currentDestination?.id == R.id.loginFragment) {
                findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            buttonLogin.setOnClickListener {
                val email = editTextEmail.text.toString().trim()
                val password = editTextPassword.text.toString().trim()
                authViewModel.login(email, password)
            }

            textViewSignUp.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
            }
        }
    }

    private fun observeViewModel() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.apply {
                buttonLogin.isEnabled = !isLoading
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

                if (isLoading) {
                    buttonLogin.text = "Signing In..."
                } else {
                    buttonLogin.text = getString(R.string.login)
                }
            }
        }

        authViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthViewModel.AuthResult.Success -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_SHORT).show()
                    // Navigation will be handled by isLoggedIn observer
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
