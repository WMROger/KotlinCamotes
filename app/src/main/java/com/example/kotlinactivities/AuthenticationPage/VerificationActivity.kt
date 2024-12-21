package com.example.kotlinactivities.AuthenticationPage

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinactivities.MainActivity
import com.example.kotlinactivities.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random
import com.example.kotlinactivities.Network.sendEmail

class VerificationActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var verificationCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        // Retrieve email and verification code from Intent
        email = intent.getStringExtra("EMAIL") ?: ""
        verificationCode = intent.getStringExtra("VERIFICATION_CODE") ?: ""

        // UI references
        val verificationCodeEditText = findViewById<EditText>(R.id.verificationCodeEditText)
        val verifyButton = findViewById<Button>(R.id.verifyButton)
        val resendCodeTextView = findViewById<TextView>(R.id.resendCode)
        val backToLoginTextView = findViewById<TextView>(R.id.backToLoginTextView)

        // Verify button action
        verifyButton.setOnClickListener {
            val enteredCode = verificationCodeEditText.text.toString().trim()

            if (enteredCode == verificationCode) {
                // Verification code matches
                Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show()

                // Mark the email as verified in Firebase Realtime Database
                markEmailAsVerified()

            } else {
                // Verification code does not match
                Toast.makeText(this, "Invalid code. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        // Resend code action
        resendCodeTextView.setOnClickListener {
            // Generate a new verification code
            verificationCode = generateVerificationCode()

            // Send email with the new verification code
            sendVerificationCodeToEmail(email, verificationCode)

            Toast.makeText(this, "New verification code sent to $email", Toast.LENGTH_SHORT).show()
        }

        // Back to login action
        backToLoginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Function to update the emailVerified status in Firebase Realtime Database
    private fun markEmailAsVerified() {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRef = database.getReference("users").child(userId)

        usersRef.child("emailVerified").setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email verification status updated.", Toast.LENGTH_SHORT).show()

                    // Redirect to LoginActivity after successful verification
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update verification status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to generate a 6-digit verification code
    private fun generateVerificationCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    // Function to send the verification code to the user's email
    private fun sendVerificationCodeToEmail(email: String, verificationCode: String) {
        val subject = "Your Verification Code"
        val messageBody = "Your new verification code is: $verificationCode"

        // Use a background thread to send the email
        Thread {
            try {
                sendEmail(email, subject, messageBody)
                runOnUiThread {
                    Toast.makeText(this, "Verification code sent to $email", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Failed to send email: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
