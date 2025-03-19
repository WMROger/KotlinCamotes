package com.example.kotlinactivities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.kotlinactivities.userPage.navBar.HomeFragment
import com.example.kotlinactivities.userPage.navBar.MapFragment
import com.example.kotlinactivities.userPage.navBar.MyRoomFragment
import com.example.kotlinactivities.userPage.navBar.ProfileFragment
import com.example.kotlinactivities.authenticationPage.LoginActivity
import com.example.kotlinactivities.model.Room
import com.google.firebase.auth.FirebaseAuth
import io.ak1.BubbleTabBar
import android.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var bubbleTabBar: BubbleTabBar
    private lateinit var auth: FirebaseAuth
    private val CALL_PHONE_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if a user is signed in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Redirect to LoginActivity if no user is logged in
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            finish()
            return
        }

        // Handle edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize BubbleTabBar
        bubbleTabBar = findViewById(R.id.bubbleTabBar)

        // Handle intent to navigate to specific fragments
        val navigateTo = intent.getStringExtra("navigateTo")
        if (navigateTo == "MyRoomFragment") {
            // Handle data passed to MyRoomFragment
            val roomTitle = intent.getStringExtra("roomTitle")
            val totalPrice = intent.getIntExtra("totalPrice", 0)

            val myRoomFragment = MyRoomFragment().apply {
                arguments = Bundle().apply {
                    putString("roomTitle", roomTitle)
                    putInt("totalPrice", totalPrice)
                }
            }

            // Navigate to MyRoomFragment
            loadFragment(myRoomFragment)
            bubbleTabBar.setSelectedWithId(R.id.myroom, false)
        } else {
            // Highlight the 'Home' tab and load HomeFragment by default
            bubbleTabBar.setSelectedWithId(R.id.home, false)
            loadFragment(HomeFragment())
        }

        // Handle tab switching with BubbleTabBar
        bubbleTabBar.addBubbleListener { id ->
            val fragment = when (id) {
                R.id.home -> HomeFragment()
                R.id.map -> MapFragment()
                R.id.myroom -> MyRoomFragment()
                R.id.profile -> ProfileFragment()
                else -> null
            }
            fragment?.let {
                loadFragment(it)
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fragment_enter, // Optional enter animation
                R.anim.fragment_exit   // Optional exit animation
            )
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Method to control bottom bar visibility
    fun setNavbarVisibility(isVisible: Boolean) {
        bubbleTabBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun makePhoneCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, make the call
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$phoneNumber")
            startActivity(callIntent)
        } else {
            // Request permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), CALL_PHONE_REQUEST_CODE)
        }
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALL_PHONE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, make the call
                makePhoneCall("1234567890")  // Example number
            } else {
                // Permission denied
                showToast("Call permission denied")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
