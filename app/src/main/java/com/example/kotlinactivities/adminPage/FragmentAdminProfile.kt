package com.example.kotlinactivities.adminPage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kotlinactivities.R
import com.example.kotlinactivities.authenticationPage.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class FragmentAdminProfile : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_profile, container, false)

        auth = FirebaseAuth.getInstance()

        // Initialize Google Sign-In Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)


        val settingsButton = view.findViewById<ImageView>(R.id.settingsButton) // Reference the settings button



        // Handle settings button click to show logout confirmation
        settingsButton.setOnClickListener {
            showLogoutConfirmationDialog() // Trigger the logout confirmation dialog
        }

        return view
    }


    private fun showLogoutConfirmationDialog() {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout_confirmation, null)

        // Create the AlertDialog
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false) // Prevent dialog from closing when tapping outside
            .create()

        // Access the dialog buttons
        val btnYes = dialogView.findViewById<Button>(R.id.btnYes)
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo)

        // Handle "Yes" button click
        btnYes.setOnClickListener {
            logoutUser()
            dialog.dismiss() // Close the dialog
        }

        // Handle "No" button click
        btnNo.setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        // Show the dialog
        dialog.show()
    }

    private fun logoutUser() {
        auth.signOut() // Sign out from Firebase

        // Sign out from Google if used
        googleSignInClient.signOut().addOnCompleteListener {
            // Navigate to login page and clear back stack
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Display a logout message
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
