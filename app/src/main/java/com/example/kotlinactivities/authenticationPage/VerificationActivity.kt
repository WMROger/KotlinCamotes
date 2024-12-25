package com.example.kotlinactivities.authenticationPage

import android.content.Intent
import android.os.Bundle
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
                        for (child in snapshot.children) {
                            val storedCode = child.child("code").value.toString()
                            if (storedCode == enteredCode) {
                                Toast.makeText(this@VerificationActivity, "Code verified successfully!", Toast.LENGTH_SHORT).show()

                                // Handle the flow based on the source
                                if (source == "register") {
                                    markEmailAsVerified()
                                } else if (source == "forget_password") {
                                    redirectToChangePassword(storedCode) // Pass the reset code to ChangePasswordActivity
                                }
                                return
                            }
                        }
                        Toast.makeText(this@VerificationActivity, "Invalid code. Please try again.", Toast.LENGTH_SHORT).show()
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
            verificationCode = generateVerificationCode()
            sendVerificationCodeToEmail(email, verificationCode)
            Toast.makeText(this, "New verification code sent to $email", Toast.LENGTH_SHORT).show()
        }

        // Back to login action
        backToLoginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun markEmailAsVerified() {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRef = database.getReference("users").child(userId)

        usersRef.child("emailVerified").setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email verification status updated.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update verification status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun redirectToChangePassword(resetCode: String) { // Accept resetCode as a parameter
        val intent = Intent(this, ChangePasswordActivity::class.java)
        intent.putExtra("EMAIL", email)
        intent.putExtra("RESET_CODE", resetCode) // Pass the verified reset code
        startActivity(intent)
        finish()
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
}
