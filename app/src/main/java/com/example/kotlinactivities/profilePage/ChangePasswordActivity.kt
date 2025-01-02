package com.example.kotlinactivities.profilePage

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.text.method.TransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinactivities.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isOldPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // UI references
        val oldPasswordEditText = findViewById<EditText>(R.id.oldPasswordEditText)
        val newPasswordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val changePasswordButton = findViewById<Button>(R.id.continueButton)
        val backToSettings = findViewById<TextView>(R.id.backtoSettingsView)

        val oldPasswordShowText = findViewById<TextView>(R.id.oldPasswordShowText)
        val newPasswordShowText = findViewById<TextView>(R.id.passwordShowTextView)
        val confirmPasswordShowText = findViewById<TextView>(R.id.confirmPasswordShowTextView)

        // Back to settings
        backToSettings.setOnClickListener {
            finish()
        }

        // Toggle visibility for old password
        oldPasswordShowText.setOnClickListener {
            isOldPasswordVisible = !isOldPasswordVisible
            togglePasswordVisibility(oldPasswordEditText, isOldPasswordVisible, oldPasswordShowText)
        }

        // Toggle visibility for new password
        newPasswordShowText.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(newPasswordEditText, isNewPasswordVisible, newPasswordShowText)
        }

        // Toggle visibility for confirm password
        confirmPasswordShowText.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(confirmPasswordEditText, isConfirmPasswordVisible, confirmPasswordShowText)
        }

        // Handle change password button click
        changePasswordButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // Validate input
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Reauthenticate with old password
            val user = auth.currentUser
            if (user != null && user.email != null) {
                val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            // Update to new password
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "Password changed successfully.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // Finish the activity or redirect to another page
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Failed to change password: ${updateTask.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            Toast.makeText(
                                this,
                                "Authentication failed. Incorrect old password.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Toggle password visibility
    private fun togglePasswordVisibility(
        editText: EditText,
        isVisible: Boolean,
        toggleTextView: TextView
    ) {
        if (isVisible) {
            editText.transformationMethod = null // Show password
            toggleTextView.text = "Hide"
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance() // Hide password
            toggleTextView.text = "Show"
        }
        // Move cursor to the end of text
        editText.setSelection(editText.text.length)
    }
}
