package com.example.kotlinactivities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {

    private lateinit var bubbleTabBar: BubbleTabBar
    private lateinit var auth: FirebaseAuth

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
}
