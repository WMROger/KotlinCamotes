package com.example.kotlinactivities.authenticationPage

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinactivities.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        auth = FirebaseAuth.getInstance()

        // Get email and reset code passed from VerificationActivity
        val email = intent.getStringExtra("EMAIL") ?: ""
        val resetCode = intent.getStringExtra("RESET_CODE") ?: ""

        // UI references
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val passwordShowTextView = findViewById<TextView>(R.id.passwordShowTextView)
        val confirmPasswordShowTextView = findViewById<TextView>(R.id.confirmPasswordShowTextView)
        val continueButton = findViewById<Button>(R.id.continueButton)

        // Toggle password visibility for passwordEditText
        passwordShowTextView.setOnClickListener {
            togglePasswordVisibility(passwordEditText, passwordShowTextView)
        }

        // Toggle password visibility for confirmPasswordEditText
        confirmPasswordShowTextView.setOnClickListener {
            togglePasswordVisibility(confirmPasswordEditText, confirmPasswordShowTextView)
        }

        // Continue button action
        continueButton.setOnClickListener {
            val newPassword = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // Validate inputs
            if (newPassword.isEmpty() || confirmPassword.isEmpty() || newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords must match and cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate reset code in Firebase
            val databaseRef = FirebaseDatabase.getInstance().reference.child("password_reset_codes")
            databaseRef.orderByChild("email").equalTo(email).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    if (snapshot.exists()) {
                        var validResetRef: String? = null
                        for (child: DataSnapshot in snapshot.children) {
                            val storedCode = child.child("code").value.toString()
                            if (storedCode == resetCode) {
                                validResetRef = child.key // Store the reference to delete later if successful
                                break
                            }
                        }

                        if (validResetRef != null) {
                            // Proceed to reset the password
                            manuallyResetPassword(email, newPassword, validResetRef, databaseRef)
                        } else {
                            Toast.makeText(this, "Invalid reset code. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "No reset code found for this email.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to validate reset code: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun manuallyResetPassword(email: String, newPassword: String, resetRef: String, databaseRef: DatabaseReference) {
        // Authenticate the user with their email and a temporary password (if applicable)
        val tempPassword = "temporaryPassword123" // Replace with actual logic if necessary
        auth.signInWithEmailAndPassword(email, tempPassword)
            .addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.updatePassword(newPassword)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                // Only delete the reset code after successful password update
                                databaseRef.child(resetRef).removeValue()
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { deleteError: Exception ->
                                        Toast.makeText(this, "Password updated, but failed to clean up reset code: ${deleteError.message}", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                Toast.makeText(this, "Error updating password: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Failed to authenticate: ${signInTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun togglePasswordVisibility(editText: EditText, toggleTextView: TextView) {
        if (editText.transformationMethod is PasswordTransformationMethod) {
            // Show the password
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            toggleTextView.text = "Hide"
        } else {
            // Hide the password
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            toggleTextView.text = "Show"
        }
        // Move the cursor to the end of the text
        editText.setSelection(editText.text.length)
    }
}
