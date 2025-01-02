package com.example.kotlinactivities.profilePage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinactivities.R
import com.example.kotlinactivities.authenticationPage.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        // UI references
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val resetPasswordButton = findViewById<Button>(R.id.continueButton)

        // Get the email and verification code from the intent
        val email = intent.getStringExtra("EMAIL") ?: ""
        val verificationCode = intent.getStringExtra("RESET_CODE") ?: ""

        // Debug intent values
        Log.d("ChangePasswordActivity", "Email: $email")
        Log.d("ChangePasswordActivity", "Verification Code from Intent: $verificationCode")

        resetPasswordButton.setOnClickListener {
            val newPassword = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // Validate inputs
            if (newPassword.isEmpty() || confirmPassword.isEmpty() || newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords must match and cannot be empty.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check the verification code in Firebase Realtime Database
            val database = FirebaseDatabase.getInstance().getReference("password_reset_codes")
            database.orderByChild("email").equalTo(email)
                .get()
                .addOnSuccessListener { snapshot ->
                    val resetEntry = snapshot.children.firstOrNull()
                    if (resetEntry == null) {
                        Toast.makeText(this, "No reset entry found for this email.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val savedCode = resetEntry.child("code").value.toString()
                    Log.d("ChangePasswordActivity", "Saved Code from Firebase: $savedCode")

                    if (verificationCode.trim() == savedCode.trim()) {
                        // Code matches, reset the password
                        val auth = FirebaseAuth.getInstance()
                        val user = auth.currentUser

                        if (user != null) {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(this, "Password reset successfully.", Toast.LENGTH_SHORT).show()

                                        // Remove the reset code from the database
                                        resetEntry.ref.removeValue()

                                        // Redirect to login
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Failed to reset password: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "User not logged in. Cannot reset password.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Invalid verification code.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error verifying reset code: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
