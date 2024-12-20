package com.example.kotlinactivities.AuthenticationPage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinactivities.R
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // UI references
        val nameEditText = findViewById<EditText>(R.id.registerNameEditText)
        val phoneNumberEditText = findViewById<EditText>(R.id.registerPhoneNumberEditText)
        val emailEditText = findViewById<EditText>(R.id.registerEmailEditText)
        val emailSuffixTextView = findViewById<TextView>(R.id.emailSuffixTextView)
        val passwordEditText = findViewById<EditText>(R.id.registerPasswordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.registerConfirmPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val backToLoginTextView = findViewById<TextView>(R.id.backToLoginTextView)

        // Register button action
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim() + emailSuffixTextView.text.toString()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (name.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Register the user with Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Navigate back to Login
        backToLoginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
