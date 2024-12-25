package com.example.kotlinactivities.authenticationPage

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kotlinactivities.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.kotlinactivities.network.sendEmail

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Realtime Database reference
        val database = FirebaseDatabase.getInstance().getReference("users")

        // UI references
        val nameEditText = findViewById<EditText>(R.id.registerNameEditText)
        val phoneNumberEditText = findViewById<EditText>(R.id.registerPhoneNumberEditText)
        val emailEditText = findViewById<EditText>(R.id.registerEmailEditText)
        val emailSuffixTextView = findViewById<TextView>(R.id.emailSuffixTextView)
        val passwordEditText = findViewById<EditText>(R.id.registerPasswordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.registerConfirmPasswordEditText)
        val passwordShowTextView = findViewById<TextView>(R.id.passwordShowTextView)
        val confirmPasswordShowTextView = findViewById<TextView>(R.id.confirmPasswordShowTextView)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val backToLoginTextView = findViewById<TextView>(R.id.backToLoginTextView)

        // Toggle password visibility for Password field
        passwordShowTextView.setOnClickListener {
            togglePasswordVisibility(passwordEditText, passwordShowTextView)
        }

        // Toggle password visibility for Confirm Password field
        confirmPasswordShowTextView.setOnClickListener {
            togglePasswordVisibility(confirmPasswordEditText, confirmPasswordShowTextView)
        }

        // Register button action
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim() + emailSuffixTextView.text.toString()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // Validate inputs
            if (name.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!phoneNumber.matches(Regex("^\\d{10}$"))) {
                Toast.makeText(this, "Phone number must be 10 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$")
            if (!password.matches(passwordRegex)) {
                Toast.makeText(
                    this,
                    "Password must be at least 8 characters, include upper and lower case letters, a number, and a special character",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Generate verification code and timestamp
            val verificationCode = Random.nextInt(100000, 999999).toString()
            val timestamp = System.currentTimeMillis()

            // Save the verification code and timestamp to Firebase
            val codeData = mapOf(
                "code" to verificationCode,
                "email" to email,
                "timestamp" to timestamp
            )
            val resetCodesDatabase = FirebaseDatabase.getInstance().getReference("password_reset_codes")
            resetCodesDatabase.push().setValue(codeData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Send verification email
                        sendEmailVerification(email, verificationCode) {
                            // Register the user in Firebase Authentication
                            registerUser(name, phoneNumber, email, password)
                        }
                    } else {
                        Toast.makeText(this, "Failed to save verification code: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Navigate back to Login
        backToLoginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun sendEmailVerification(email: String, verificationCode: String, onComplete: () -> Unit) {
        val subject = "Your Verification Code"
        val messageBody = "Your verification code is: $verificationCode"

        lifecycleScope.launch {
            try {
                sendEmail(email, subject, messageBody)
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Verification code sent to $email", Toast.LENGTH_LONG).show()
                    onComplete()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Failed to send email: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun registerUser(name: String, phoneNumber: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user!!.uid
                    val userMap = mapOf(
                        "name" to name,
                        "phoneNumber" to phoneNumber,
                        "email" to email,
                        "emailVerified" to false,
                        "role" to "User" // Assign default role as "User"
                    )
                    val database = FirebaseDatabase.getInstance().getReference("users")
                    database.child(userId).setValue(userMap).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            val intent = Intent(this, VerificationActivity::class.java)
                            intent.putExtra("EMAIL", email)
                            intent.putExtra("SOURCE", "register")
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Database Error: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Authentication Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    private fun togglePasswordVisibility(editText: EditText, toggleTextView: TextView) {
        if (editText.transformationMethod is PasswordTransformationMethod) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            toggleTextView.text = "Hide"
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            toggleTextView.text = "Show"
        }
        toggleTextView.paintFlags = toggleTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        editText.setSelection(editText.text.length)
    }
}
