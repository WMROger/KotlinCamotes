package com.example.kotlinactivities.homePage

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinactivities.R

class AdminMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        val viewUsersButton = findViewById<Button>(R.id.viewUsersButton)
        val manageSettingsButton = findViewById<Button>(R.id.manageSettingsButton)

        viewUsersButton.setOnClickListener {
            // Example: Navigate to ViewUsersActivity (not yet created)
            Toast.makeText(this, "View Users clicked!", Toast.LENGTH_SHORT).show()
        }

        manageSettingsButton.setOnClickListener {
            // Example: Navigate to ManageSettingsActivity (not yet created)
            Toast.makeText(this, "Manage Settings clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}
