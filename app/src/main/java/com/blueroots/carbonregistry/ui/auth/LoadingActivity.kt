package com.blueroots.carbonregistry.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.blueroots.carbonregistry.MainActivity
import com.blueroots.carbonregistry.R

class LoadingActivity : AppCompatActivity() {

    private val LOADING_DELAY = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        // Check auth and navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthAndNavigate()
        }, LOADING_DELAY)
    }

    private fun checkAuthAndNavigate() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)

        // Always go to MainActivity - it will handle the navigation logic
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("is_logged_in", isLoggedIn)
        startActivity(intent)
        finish()

        // Smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
