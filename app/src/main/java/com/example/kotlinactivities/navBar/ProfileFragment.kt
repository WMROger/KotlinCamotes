package com.example.kotlinactivities.navBar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kotlinactivities.R
import com.example.kotlinactivities.profilePage.SettingsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Get the currently logged-in user
        val currentUser = auth.currentUser
        val profileName = view.findViewById<TextView>(R.id.profile_name)
        val profileContact = view.findViewById<TextView>(R.id.profile_contact)
        val profileEmail = view.findViewById<TextView>(R.id.profile_email)
        val settingsButton = view.findViewById<View>(R.id.settingsButton) // Settings button

        // Check if the user is logged in
        currentUser?.let { user ->
            val uid = user.uid // Get the user ID
            val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid)

            // Fetch data from the database
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get the user data from the database
                        val fullName = snapshot.child("name").getValue(String::class.java) ?: "No Name Available"
                        val email = snapshot.child("email").getValue(String::class.java) ?: "No Email Available"
                        var phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: "No Number Available"

                        // Prepend "0" to the phone number if it doesn't already start with "0"
                        if (!phoneNumber.startsWith("0") && phoneNumber != "No Number Available") {
                            phoneNumber = "0$phoneNumber"
                        }

                        // Display the data in the UI
                        val firstName = fullName.split(" ").getOrNull(0) ?: "No Name Available" // Get first name
                        profileName.text = firstName
                        profileEmail.text = email
                        profileContact.text = phoneNumber
                    } else {
                        Toast.makeText(requireContext(), "User data not found.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to fetch data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Navigate to SettingsFragment when the settings button is clicked
        settingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment()) // Replace with SettingsFragment
                .addToBackStack(null) // Add to back stack for navigation
                .commit()
        }

        return view
    }
}
