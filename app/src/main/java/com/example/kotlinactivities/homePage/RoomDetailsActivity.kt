package com.example.kotlinactivities.homePage

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.kotlinactivities.R
import com.example.kotlinactivities.databinding.ActivityRoomDetailsBinding
import com.example.kotlinactivities.model.Room
import java.text.NumberFormat
import java.util.*

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
        room?.let { roomData ->
            binding.roomImage.setImageResource(roomData.imageUrl)
            binding.roomTitle.text = roomData.title
            binding.roomLocation.text = "Himensulan Island, Camotes Cebu" // Static for now
            binding.roomRating.text = roomData.rating
            binding.roomPrice.text = formatPrice(removeNightSuffix(roomData.price)) // Format price
            binding.roomDescription.text =
                "Indulge in luxury and comfort in our ${roomData.title}, featuring elegant interiors, plush bedding, a spacious seating area, and modern amenities."

            // Book button action
            binding.bookButton.setOnClickListener {
                // Create an intent to navigate to BookingRoomActivity
                val intent = Intent(this, BookingRoomActivity::class.java)

                // Pass data to the BookingRoomActivity
                intent.putExtra("roomTitle", roomData.title) // Pass room title
                intent.putExtra("roomPrice", removeNightSuffix(roomData.price).toInt()) // Pass room price as integer
                intent.putExtra("roomType", roomData.title) // Pass the room type dynamically
                intent.putExtra("paxCount", roomData.people?.toInt() ?: 2) // Pass the number of people (converted to Int)
                intent.putExtra("imageUrl", roomData.imageUrl) // Pass the image URL

                // Start the BookingRoomActivity
                startActivity(intent)
            }
        }

        // Back button action: Go back to HomeFragment
        binding.backButton.setOnClickListener {
            finish() // Ends the current activity and returns to the previous one
        }

        // Heart button action: Toggle favorite state
        binding.heartButton.setOnClickListener {
            toggleFavorite()
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

    // Helper function to remove `/night` and `₱` from the price
    private fun removeNightSuffix(price: String?): String {
        return price?.replace("/night", "")?.replace("₱", "")?.replace(",", "")?.trim() ?: "0"
    }

    // Helper function to format price with comma and peso sign
    private fun formatPrice(price: String): String {
        val priceValue = price.toIntOrNull() ?: 0
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.maximumFractionDigits = 0
        return "₱${formatter.format(priceValue)}"
    }
}
