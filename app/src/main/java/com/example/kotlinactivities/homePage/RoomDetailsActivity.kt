package com.example.kotlinactivities.homePage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinactivities.R
import com.example.kotlinactivities.databinding.ActivityRoomDetailsBinding
import com.example.kotlinactivities.model.Room

class RoomDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomDetailsBinding

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

        // Book button action (placeholder)
        binding.bookButton.setOnClickListener {
            // Implement booking action
        }
    }
}
