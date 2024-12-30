package com.example.kotlinactivities.profilePage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kotlinactivities.MainActivity
import com.example.kotlinactivities.R
import com.example.kotlinactivities.authenticationPage.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SettingsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Hide the bottom navbar when the SettingsFragment is visible
        (requireActivity() as MainActivity).setNavbarVisibility(false)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Get the currently logged-in user's UID
        val currentUserId = auth.currentUser?.uid

        // Find views to display user information
        val accountName = view.findViewById<TextView>(R.id.account_name)
        val accountEmail = view.findViewById<TextView>(R.id.account_email)

        // Query the database to fetch user details
        currentUserId?.let { uid ->
            val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Fetch user details
                    val name = snapshot.child("name").value.toString()
                    val email = snapshot.child("email").value.toString()

                    // Update UI with user details
                    accountName.text = name
                    accountEmail.text = email
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load user data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Handle back button click
        val backButton = view.findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            // Show the bottom navbar again when navigating back
            (requireActivity() as MainActivity).setNavbarVisibility(true)
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Handle logout button click
        val logoutButton = view.findViewById<TextView>(R.id.logout_button)
        logoutButton.setOnClickListener {
            logoutUser()
        }

        return view
    }

    private fun logoutUser() {
        // Sign out the user from FirebaseAuth
        auth.signOut()

        // Navigate to the LoginActivity after logout
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // Display a logout message
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show the bottom navbar when the fragment is destroyed
        (requireActivity() as MainActivity).setNavbarVisibility(true)
    }
}
