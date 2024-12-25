package com.example.kotlinactivities.authenticationPage

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kotlinactivities.R
import kotlin.random.Random
import com.example.kotlinactivities.network.sendEmail
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class ForgetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        // UI references
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val sendResetLinkButton = findViewById<Button>(R.id.sendResetLinkButton)
        val backToLoginTextView = findViewById<TextView>(R.id.backToLoginTextView)

        // Set up the "Send Reset Link" button
        sendResetLinkButton.setOnClickListener {
            var email = emailEditText.text.toString().trim()

            // Validate email input
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Append @gmail.com to the email if needed
            if (!email.contains("@")) {
                email += "@gmail.com"
            }

            // Generate a 6-digit verification code
            val verificationCode = Random.nextInt(100000, 999999).toString()

            // Save the reset code in Firebase Realtime Database
            val database = FirebaseDatabase.getInstance().getReference("password_reset_codes")
            val resetData = mapOf(
                "email" to email,
                "code" to verificationCode,
                "timestamp" to System.currentTimeMillis()
            )

            database.push().setValue(resetData)
                .addOnSuccessListener {
                    lifecycleScope.launch {
                        try {
                            val subject = "Your Password Reset Code"
                            val messageBody = "Your password reset code is: $verificationCode"
                            sendEmail(email, subject, messageBody)

                            // Redirect to VerificationActivity
                            val intent = Intent(this@ForgetPasswordActivity, VerificationActivity::class.java)
                            intent.putExtra("EMAIL", email)
                            intent.putExtra("RESET_CODE", verificationCode)
                            intent.putExtra("SOURCE", "forget_password")
                            startActivity(intent)
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(this@ForgetPasswordActivity, "Failed to send email: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to store reset code: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Set up "Back to Login" action
        backToLoginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
