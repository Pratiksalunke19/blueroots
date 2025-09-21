package com.blueroots.carbonregistry

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.blueroots.carbonregistry.databinding.ActivityMainBinding
import com.blueroots.carbonregistry.viewmodel.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        // Apply theme before creating UI
        applyTheme()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeAuthState()
    }

    private fun applyTheme() {
        val sharedPrefs = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav: BottomNavigationView = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)

        // Handle bottom nav visibility based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.signUpFragment -> {
                    bottomNav.visibility = BottomNavigationView.GONE
                }
                else -> {
                    bottomNav.visibility = BottomNavigationView.VISIBLE
                }
            }
        }
    }

    private fun observeAuthState() {
        authViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            val currentDestination = navController.currentDestination?.id

            if (!isLoggedIn) {
                // Only navigate to login if not already on auth screens
                if (currentDestination != R.id.loginFragment && currentDestination != R.id.signUpFragment) {
                    navController.navigate(R.id.loginFragment)
                }
            } else {
                // User is logged in - navigate to profile if on auth screens
                if (currentDestination == R.id.loginFragment || currentDestination == R.id.signUpFragment) {
                    navController.navigate(R.id.profileFragment)
                }
            }
        }
    }
}
