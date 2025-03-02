package com.example.kotlinactivities.adminPage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.kotlinactivities.BaseActivity
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminPage.ApprovalFragment
import com.example.kotlinactivities.adminPage.adminRoom.AddRoomFragment
import com.example.kotlinactivities.authenticationPage.LoginActivity
import com.example.kotlinactivities.userPage.navBar.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import io.ak1.BubbleTabBar

class AdminMainActivity : BaseActivity() {

    private lateinit var bubbleTabBar: BubbleTabBar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize BubbleTabBar
        bubbleTabBar = findViewById(R.id.adminBubbleTabBar)

        // Load the ApprovalFragment by default
        loadFragment(ApprovalFragment())
        bubbleTabBar.setSelectedWithId(R.id.admin_dashboard, true) // Set default tab explicitly

        // Handle tab switching using BubbleTabBar
        bubbleTabBar.addBubbleListener { id ->
            val fragment = when (id) {
                R.id.admin_dashboard -> ApprovalFragment()
                R.id.admin_add_room -> AddRoomFragment()
                R.id.admin_settings -> AdminAnalyticsFragment() // Replace with the correct fragment if necessary
                R.id.admin_profile -> ProfileFragment()
                else -> null
            }
            fragment?.let { loadFragment(it) }
        }

        // Handle Logout Button
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            logoutUser()
        }
    }

    // Function to load selected fragments
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fragment_enter, // Optional enter animation
                R.anim.fragment_exit   // Optional exit animation
            )
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Logout functionality with confirmation dialog
    private fun logoutUser() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout_confirmation, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val btnYes = dialogView.findViewById<Button>(R.id.btnYes)
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
