package com.example.kotlinactivities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Access SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("SplashPrefs", MODE_PRIVATE)
        val currentSplash = sharedPreferences.getInt("currentSplash", 1)

        // Load the appropriate splash layout
        when (currentSplash) {
            1 -> setContentView(R.layout.splash_screen1)
            2 -> setContentView(R.layout.splash_screen2)
            3 -> setContentView(R.layout.splash_screen3)
        }

        // Determine the next splash screen
        val nextSplash = if (currentSplash < 3) currentSplash + 1 else 1
        sharedPreferences.edit().putInt("currentSplash", nextSplash).apply()

        // Delay and navigate to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the SplashActivity
        }, 3000) // 3-second delay
    }
}
