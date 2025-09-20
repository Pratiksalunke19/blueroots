package com.blueroots.carbonregistry

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.blueroots.carbonregistry.databinding.ActivityMainBinding
import com.blueroots.carbonregistry.ui.registration.ProjectRegistrationFragment
import com.blueroots.carbonregistry.ui.monitoring.MonitoringUploadFragment
import com.blueroots.carbonregistry.ui.credits.CreditIssuanceFragment
import com.blueroots.carbonregistry.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // IMPORTANT: Install splash screen before super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProjectRegistrationFragment())
                .commit()
        }

        setupBottomNavigation()
        // REMOVED setupToolbar() temporarily
    }

    // COMMENTED OUT TOOLBAR SETUP FOR NOW
    /*
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "BlueRoots"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_search -> {
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_more -> {
                Toast.makeText(this, "More options", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    */

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_registration -> ProjectRegistrationFragment()
                R.id.nav_monitoring -> MonitoringUploadFragment()
                R.id.nav_credits -> CreditIssuanceFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> ProjectRegistrationFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()

            true
        }
    }
}
