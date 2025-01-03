package com.example.kotlinactivities.adminPage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.kotlinactivities.R
import com.example.kotlinactivities.authenticationPage.LoginActivity
import com.example.kotlinactivities.admin.AdminDashboardFragment
import com.example.kotlinactivities.admin.ViewUsersFragment
import com.example.kotlinactivities.admin.ManageSettingsFragment
import com.example.kotlinactivities.navBar.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import io.ak1.BubbleTabBar

class AdminMainActivity : AppCompatActivity() {

    private lateinit var bubbleTabBar: BubbleTabBar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize BubbleTabBar
        bubbleTabBar = findViewById(R.id.adminBubbleTabBar)

        // Load DashboardFragment by default
        bubbleTabBar.setSelectedWithId(R.id.admin_dashboard, false)
        loadFragment(AdminDashboardFragment())

        // Handle tab switching using BubbleTabBar
        bubbleTabBar.addBubbleListener { id ->
            val fragment = when (id) {
                R.id.admin_dashboard -> AdminDashboardFragment()
                R.id.admin_users -> ViewUsersFragment()
                R.id.admin_settings -> ManageSettingsFragment()
                R.id.admin_profile -> ProfileFragment() // Use a generic profile fragment or an admin-specific one
                else -> null
            }
            fragment?.let {
                loadFragment(it)
            }
        }

        // Button-based navigation setup
        val viewUsersButton = findViewById<Button>(R.id.viewUsersButton)
        val manageSettingsButton = findViewById<Button>(R.id.manageSettingsButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        // Show ViewUsersFragment when "View Users" button is clicked
        viewUsersButton.setOnClickListener {
            loadFragment(ViewUsersFragment())
        }

        // Show ManageSettingsFragment when "Manage Settings" button is clicked
        manageSettingsButton.setOnClickListener {
            loadFragment(ManageSettingsFragment())
        }

        // Logout functionality with confirmation dialog
        logoutButton.setOnClickListener {
            logoutUser() // Call the logout confirmation dialog
        }
    }

    // Load the selected fragment
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
        // Inflate the custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout_confirmation, null)

        // Create the AlertDialog
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Prevent dialog from closing when tapping outside
            .create()

        // Access the dialog buttons
        val btnYes = dialogView.findViewById<Button>(R.id.btnYes)
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo)

        // Handle "Yes" button click
        btnYes.setOnClickListener {
            // Perform logout
            auth.signOut()

            // Navigate to the LoginActivity after logout
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            // Close the dialog
            dialog.dismiss()
        }

        // Handle "No" button click
        btnNo.setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        // Show the dialog
        dialog.show()
    }
}
