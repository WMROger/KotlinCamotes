package com.example.kotlinactivities.authenticationPage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kotlinactivities.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.kotlinactivities.network.sendEmail

class VerificationActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var verificationCode: String
    private lateinit var source: String // Determines if the source is "register" or "forget_password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        // Retrieve email, verification code, and source flag from Intent
        email = intent.getStringExtra("EMAIL") ?: ""
        verificationCode = intent.getStringExtra("VERIFICATION_CODE") ?: ""
        source = intent.getStringExtra("SOURCE") ?: ""

        Log.d("VerificationActivity", "Verification Code from Intent: $verificationCode")

        // UI references
        val verificationCodeEditText = findViewById<EditText>(R.id.verificationCodeEditText)
        val verifyButton = findViewById<Button>(R.id.verifyButton)
        val resendCodeTextView = findViewById<TextView>(R.id.resendCode)
        val backToLoginTextView = findViewById<TextView>(R.id.backToLoginTextView)

        // Verify button action
        verifyButton.setOnClickListener {
            val enteredCode = verificationCodeEditText.text.toString().trim()

            if (enteredCode.isEmpty()) {
                Toast.makeText(this, "Please enter the verification code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check the code in Firebase
            val database = FirebaseDatabase.getInstance().getReference("password_reset_codes")
            database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var codeMatched = false
                        val currentTime = System.currentTimeMillis()
                        val expirationTime = 2 * 60 * 1000 // 2 minutes in milliseconds

                        for (child in snapshot.children) {
                            val storedCode = child.child("code").value.toString().trim()
                            val timestamp = child.child("timestamp").value as? Long ?: 0L // Retrieve the timestamp

                            if (currentTime - timestamp > expirationTime) {
                                // Code is expired, delete it from the database
                                child.ref.removeValue()
                                Log.d("VerificationActivity", "Expired code deleted for email: $email")
                                continue
                            }

                            Log.d("VerificationActivity", "Stored Code from Firebase: $storedCode")
                            Log.d("VerificationActivity", "Entered Code: $enteredCode")
                            if (storedCode == enteredCode.trim()) {
                                codeMatched = true
                                Toast.makeText(this@VerificationActivity, "Code verified successfully!", Toast.LENGTH_SHORT).show()

                                // Delete the code after successful verification
                                child.ref.removeValue()

                                // Proceed based on the source
                                if (source == "register") {
                                    markEmailAsVerified()
                                } else if (source == "forget_password") {
                                    sendResetLinkToEmail(email)
                                    finish()
                                }
                                break
                            }
                        }
                        if (!codeMatched) {
                            Toast.makeText(this@VerificationActivity, "Invalid or expired code. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@VerificationActivity, "No reset code found for this email.", Toast.LENGTH_SHORT).show()
                    }
                }



                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@VerificationActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Resend code action
        resendCodeTextView.setOnClickListener {
            val database = FirebaseDatabase.getInstance().getReference("password_reset_codes")
            val currentTime = System.currentTimeMillis()
            val expirationTime = 2 * 60 * 1000 // 2 minutes in milliseconds

            database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { child ->
                        val timestamp = child.child("timestamp").value as? Long ?: 0L
                        if (currentTime - timestamp > expirationTime) {
                            // Delete expired code
                            child.ref.removeValue()
                        }
                    }

                    // Generate and send a new code
                    verificationCode = generateVerificationCode()
                    val newCodeData = mapOf(
                        "code" to verificationCode,
                        "email" to email,
                        "timestamp" to System.currentTimeMillis()
                    )

                    database.push().setValue(newCodeData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                sendVerificationCodeToEmail(email, verificationCode)
                                Toast.makeText(this@VerificationActivity, "New verification code sent to $email", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@VerificationActivity, "Failed to generate new code: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@VerificationActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }


        // Back to login action
        backToLoginTextView.setOnClickListener {
            finish()
        }
    }

    private fun sendResetLinkToEmail(email: String) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset link sent to $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send reset link: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun generateVerificationCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    private fun sendVerificationCodeToEmail(email: String, verificationCode: String) {
        val subject = "Your Verification Code"
        val messageBody = "Your new verification code is: $verificationCode"

        lifecycleScope.launch {
            try {
                sendEmail(email, subject, messageBody)
                Toast.makeText(this@VerificationActivity, "Verification code sent to $email", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@VerificationActivity, "Failed to send email: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun markEmailAsVerified() {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val usersRef = database.getReference("users").child(userId)

            usersRef.child("emailVerified").setValue(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email verification status updated.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to update verification status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "User is not logged in. Cannot update email verification status.", Toast.LENGTH_SHORT).show()
        }
    }

    // Uncomment this method to enable redirecting to ChangePasswordActivity
    /*
    private fun redirectToChangePassword(resetCode: String) {
        val intent = Intent(this, ChangePasswordActivity::class.java)
        intent.putExtra("EMAIL", email)
        intent.putExtra("RESET_CODE", resetCode) // Pass the verified reset code
        startActivity(intent)
        finish()
    }
    */
}
