package com.example.kotlinactivities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airbnb.lottie.LottieAnimationView
import com.example.kotlinactivities.adminPage.AdminMainActivity
import com.example.kotlinactivities.authenticationPage.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Set status bar and navigation bar colors to white
        window.statusBarColor = resources.getColor(android.R.color.white, theme)
        window.navigationBarColor = resources.getColor(android.R.color.white, theme)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Start Lottie animation
        val lottieAnimationView = findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        lottieAnimationView.playAnimation()

        // Delay for the Lottie animation, then proceed
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000) // Duration of the animation
//            navigateToOnboarding()
            checkFirstTimeUser() // Check if it's the first-time user or navigate based on role
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
            // Redirect to onboarding screens (first-time user)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstTime", false) // Set the flag to false
            editor.apply()

            val intent = Intent(this, OnboardingActivity::class.java) // New Onboarding Screens
            startActivity(intent)
            finish()
        } else {
            // Check if a user is logged in and navigate based on role
            checkUserSession()
        }
    }

    private fun checkUserSession() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // If no user is logged in, redirect to LoginActivity
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            finish()
            return
        }

        // Fetch the user's role from Firebase
        val userId = currentUser.uid
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users").child(userId)

        usersRef.child("role").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val role = task.result?.value.toString()

                when (role) {
                    "Admin" -> {
                        // Redirect to AdminMainActivity
                        val intent = Intent(this, AdminMainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    "User" -> {
                        // Redirect to MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    else -> {
                        // If the role is unknown, sign out and redirect to LoginActivity
                        auth.signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
            } else {
                // Handle error fetching role
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            finish() // End SplashActivity
        }
    }
}
