package com.example.kotlinactivities.homePage

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.kotlinactivities.R
import com.example.kotlinactivities.databinding.ActivityRoomDetailsBinding
import com.example.kotlinactivities.navBar.HomeFragment
import com.example.kotlinactivities.model.Room

class RoomDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomDetailsBinding
    private var isFavorited = false // State to track if the heart is favorited

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityRoomDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get Room data from intent
        val room = intent.getParcelableExtra<Room>("room")

        // Populate Room Details
        room?.let {
            binding.roomImage.setImageResource(it.imageUrl)
            binding.roomTitle.text = it.title
            binding.roomLocation.text = "Himensulan Island, Camotes Cebu" // Static for now
            binding.roomRating.text = it.rating
            binding.roomPrice.text = it.price
            binding.roomDescription.text =
                "Indulge in luxury and comfort in our Deluxe Room, featuring elegant interiors, plush bedding, a spacious seating area, and modern amenities."
        }

        // Back button action: Go back to HomeFragment
        binding.backButton.setOnClickListener {
            finish() // Ends the current activity and returns to the previous one
        }

        // Heart button action: Toggle favorite state
        binding.heartButton.setOnClickListener {
            toggleFavorite()
        }

        // Book button action (placeholder)
        binding.bookButton.setOnClickListener {
            // Implement booking action
        }
    }

    // Function to toggle favorite state
    private fun toggleFavorite() {
        isFavorited = !isFavorited // Toggle the state
        val heartDrawable = if (isFavorited) {
            R.drawable.ic_heart // Red heart drawable when favorited
        } else {
            R.drawable.ic_heart_black // Black heart drawable when not favorited
        }

        binding.heartButton.setImageDrawable(ContextCompat.getDrawable(this, heartDrawable))
    }
}
