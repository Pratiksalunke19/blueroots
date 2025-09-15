package com.blueroots.carbonregistry

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.blueroots.carbonregistry.databinding.ActivityMainBinding
import com.blueroots.carbonregistry.ui.registration.ProjectRegistrationFragment
import com.blueroots.carbonregistry.ui.monitoring.MonitoringUploadFragment
import com.blueroots.carbonregistry.ui.credits.CreditIssuanceFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProjectRegistrationFragment())
                .commit()
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_registration -> ProjectRegistrationFragment()
                R.id.nav_monitoring -> MonitoringUploadFragment()
                R.id.nav_credits -> CreditIssuanceFragment()
                else -> ProjectRegistrationFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()

            true
        }
    }
}
