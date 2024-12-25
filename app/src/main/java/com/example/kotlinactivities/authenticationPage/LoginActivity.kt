package com.example.kotlinactivities.authenticationPage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinactivities.MainActivity
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminPage.AdminMainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in and redirect based on role
        checkUserSession()

        // UI References
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)
        val registerTextView = findViewById<TextView>(R.id.RegisterAccount)
        val googleSignInButton = findViewById<LinearLayout>(R.id.googleSignInButton)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google Sign-In Button
        googleSignInButton.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }

        // Redirect to RegisterActivity
        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Redirect to ForgetPasswordActivity
        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }

        // Handle Login Button
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        handleUserLogin()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Check if the user is already logged in and redirect accordingly
    private fun checkUserSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            handleUserLogin() // Automatically redirect based on role
        }
    }

    // Handle User Login Based on Role
    private fun handleUserLogin() {
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users").child(userId)

        // Check the role of the logged-in user
        usersRef.child("role").get().addOnCompleteListener { dbTask ->
            if (dbTask.isSuccessful) {
                val result = dbTask.result
                if (result.exists()) {
                    val role = result.value.toString()
                    Log.d("FirebaseDebug", "User role: $role")
                    if (role == "Admin") {
                        // Redirect to AdminMainActivity
                        val intent = Intent(this, AdminMainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Redirect to MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Error: Role does not exist. Please contact support.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error retrieving role: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle Google Sign-In Result
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            } else {
                Toast.makeText(this, "Google Sign-In canceled", Toast.LENGTH_SHORT).show()
            }
        }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(Exception::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    handleUserLogin() // Handle user login after Google sign-in
                } else {
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
