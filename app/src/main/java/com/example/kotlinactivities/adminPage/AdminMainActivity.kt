package com.example.kotlinactivities.adminPage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

        // Logout functionality
        logoutButton.setOnClickListener {
            // Sign out from Firebase
            auth.signOut()

            // Redirect to LoginActivity and clear activity stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // Helper function to replace the fragment in the container
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null) // Optional: Add to back stack for navigation
        transaction.commit()
    }
}
