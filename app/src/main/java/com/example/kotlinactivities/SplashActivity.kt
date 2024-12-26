package com.example.kotlinactivities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Set status bar and navigation bar colors to white
        window.statusBarColor = resources.getColor(android.R.color.white, theme)
        window.navigationBarColor = resources.getColor(android.R.color.white, theme)

        // Start Lottie animation
        val lottieAnimationView = findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        lottieAnimationView.playAnimation()

        // Delay and always navigate to onboarding for now
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000) // Duration of animation
            navigateToOnboarding()
//            checkFirstTimeUser()// Temporarily always navigate to onboarding
        }
    }

    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java) // Redirect to onboarding
        startActivity(intent)
        finish() // End SplashActivity
    }


    private fun checkFirstTimeUser() {
        // Check if the user is opening the app for the first time
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

        if (isFirstTime) {
            // Redirect to splash_screen1-3
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstTime", false) // Set the flag to false
            editor.apply()

            val intent = Intent(this, OnboardingActivity::class.java) // New Onboarding Screens
            startActivity(intent)
        } else {
            // Redirect to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        finish() // End SplashActivity
    }

}
