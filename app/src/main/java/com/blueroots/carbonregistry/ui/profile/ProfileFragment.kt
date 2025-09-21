package com.blueroots.carbonregistry.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.blueroots.carbonregistry.R
import com.blueroots.carbonregistry.databinding.FragmentProfileBinding
import com.blueroots.carbonregistry.utils.ThemeManager
import com.blueroots.carbonregistry.viewmodel.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var themeManager: ThemeManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        themeManager = ThemeManager.getInstance(requireContext())

        setupMenu()
        setupClickListeners()
        setupThemeSwitch()
        observeViewModel()

        // Load fresh profile data
        authViewModel.refreshUserProfile()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.profile_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_logout -> {
                        showLogoutDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupClickListeners() {
        binding.apply {
            buttonEditProfile.setOnClickListener {
                // TODO: Navigate to edit profile screen
            }

            buttonSettings.setOnClickListener {
                // TODO: Navigate to settings screen
            }

            // Add sign out button click listener
            buttonSignOut.setOnClickListener {
                showLogoutDialog()
            }
        }
    }

    private fun setupThemeSwitch() {
        binding.apply {
            switchDarkMode.isChecked = themeManager.isDarkModeEnabled()

            switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                themeManager.setDarkMode(isChecked)
            }
        }
    }

    private fun observeViewModel() {
        // Observe authentication state
        authViewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (!isLoggedIn) {
                // User has been logged out, navigate to login
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
        }

        // Observe user profile data
        authViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let { updateProfileUI(it) }
        }

        // Observe current auth user
        authViewModel.authState.observe(viewLifecycleOwner) { user ->
            user?.let { updateUserInfo(it) }
        }
    }

    private fun updateUserInfo(user: com.blueroots.carbonregistry.data.models.AuthUser) {
        binding.apply {
            textViewName.text = user.fullName.ifBlank { "User" }
            textViewEmail.text = user.email

            // Update verification status
            if (user.isVerified) {
                chipRole.text = "Verified ${user.organizationType}"
                chipRole.setChipBackgroundColorResource(R.color.status_ready)
            } else {
                chipRole.text = "Unverified Account"
                chipRole.setChipBackgroundColorResource(R.color.status_warning)
            }
        }
    }

    private fun updateProfileUI(profile: com.blueroots.carbonregistry.data.models.UserProfile) {
        binding.apply {
            textViewName.text = profile.fullName ?: "User"
            textViewEmail.text = profile.email

            profile.organizationType?.let { orgType ->
                chipRole.text = if (profile.isVerified) "Verified $orgType" else orgType
            }
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                authViewModel.logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
