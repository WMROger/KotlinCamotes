package com.example.kotlinactivities.adminPage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.kotlinactivities.R
import com.example.kotlinactivities.admin.ViewUsersFragment
import com.example.kotlinactivities.admin.ManageSettingsFragment
import com.example.kotlinactivities.authenticationPage.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class AdminMainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

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

            // Display a logout message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            dialog.dismiss() // Close the dialog
        }

        // Handle "No" button click
        btnNo.setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        // Show the dialog
        dialog.show()
    }


    // Helper function to replace the fragment in the container
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null) // Optional: Add to back stack for navigation
        transaction.commit()
    }
}
