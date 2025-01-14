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
                        Toast.makeText(
                            this,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    // Check if the user is already logged in and redirect accordingly
    private fun checkUserSession() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w("UserSession", "No current user logged in")
            return
        }

        val userId = currentUser.uid
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users").child(userId)

        usersRef.child("role").get().addOnCompleteListener { dbTask ->
            if (dbTask.isSuccessful) {
                val role = dbTask.result?.value?.toString()
                Log.d("UserSession", "User role: $role")
                when (role) {
                    "Admin" -> {
                        val intent = Intent(this, AdminMainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                Log.e("UserSession", "Failed to retrieve user role: ${dbTask.exception?.message}")
                Toast.makeText(this, "Error retrieving user role", Toast.LENGTH_SHORT).show()
            }
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
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Redirect to MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Error: Role does not exist. Please contact support.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Error retrieving role: ${dbTask.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
                Log.e("GoogleSignIn", "Google Sign-In failed or canceled. Result code: ${result.resultCode}")
                Toast.makeText(this, "Google Sign-In was canceled or failed.", Toast.LENGTH_SHORT).show()
            }
        }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(Exception::class.java)
            if (account != null) {
                Log.d("GoogleSignIn", "Google Account retrieved: ${account.email}")
                firebaseAuthWithGoogle(account)
            } else {
                Log.e("GoogleSignIn", "Google Account is null")
                Toast.makeText(this, "Google Account retrieval failed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error during Google Sign-In: ${e.message}")
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val database = FirebaseDatabase.getInstance()
                        val usersRef = database.getReference("users").child(user.uid)

                        // Check if the user already exists in the database
                        usersRef.child("role").get().addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                val roleResult = dbTask.result
                                if (roleResult.exists()) {
                                    // User exists in the database, check their role
                                    val role = roleResult.value.toString()
                                    Log.d("FirebaseDebug", "User exists with role: $role")
                                    if (role == "Admin") {
                                        // Redirect to Admin Dashboard
                                        val intent = Intent(this, AdminMainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        // Redirect to User Dashboard
                                        val intent = Intent(this, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    // User does not exist in the database, redirect to RegisterGoogleActivity
                                    Log.d("FirebaseDebug", "User does not exist, redirecting to registration")
                                    val intent = Intent(this, RegisterGoogleActivity::class.java)
                                    intent.putExtra("FROM_GOOGLE_SIGN_IN", true) // Pass flag to identify the source
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                // Handle database access failure
                                Log.e("FirebaseDebug", "Error checking user role: ${dbTask.exception?.message}")
                                Toast.makeText(this, "Error checking user details. Try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e("FirebaseDebug", "User is null after successful sign-in")
                        Toast.makeText(this, "Error: User is null. Try again.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle authentication failure
                    Log.e("FirebaseDebug", "Authentication Failed: ${task.exception?.message}")
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



}
