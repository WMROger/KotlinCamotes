package com.example.kotlinactivities.homePage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.kotlinactivities.R
import com.example.kotlinactivities.databinding.ActivityRoomDetailsBinding
import com.example.kotlinactivities.model.Room
import com.example.kotlinactivities.myRoom.CancelBookingFragment
import com.google.firebase.database.FirebaseDatabase
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
        val isFromMyRoom = intent.getBooleanExtra("isFromMyRoom", false)
        val bookingStatus = intent.getStringExtra("bookingStatus") ?: "Pending"

        // Check for null room object
        if (room == null) {
            // Handle error if room is not passed correctly
            finish()
            return
        }

        // Populate Room Details
        populateRoomDetails(room)

        // Update the button text dynamically
        updateBookingButton(isFromMyRoom, bookingStatus)

        // Back button action: Go back to the previous fragment/activity
        binding.backButton.setOnClickListener {
            finish() // Ends the current activity and returns to the previous one
        }

        // Heart button action: Toggle favorite state
        binding.heartButton.setOnClickListener {
            toggleFavorite()
        }
    }

    // Function to populate room details
    private fun populateRoomDetails(room: Room) {
        // Use Glide to load the image URL
        Glide.with(this)
            .load(room.imageUrl) // Load the image from the URL
            .placeholder(R.drawable.ic_cupids_deluxe) // Placeholder image while loading
            .error(R.drawable.ic_splash) // Fallback image in case of an error
            .into(binding.roomImage) // Bind it to the ImageView

        binding.roomTitle.text = room.title
        binding.roomLocation.text = "Himensulan Island, Camotes Cebu" // Static for now
        binding.roomRating.text = room.rating
        binding.roomPrice.text = formatPrice(removeNightSuffix(room.price)) // Format price
        binding.roomDescription.text =
            "Indulge in luxury and comfort in our ${room.title}, featuring elegant interiors, plush bedding, a spacious seating area, and modern amenities."
    }

    // Function to update the booking button text and behavior
    private fun updateBookingButton(isFromMyRoom: Boolean, bookingStatus: String) {
        if (isFromMyRoom) {
            binding.extendStayButton.visibility = View.VISIBLE // Show Extend Stay button
            binding.cancelButton.visibility = View.VISIBLE // Show Cancel button
            binding.bookButton.visibility = View.GONE // Hide the Book Now button

            // Check the booking status
            if (bookingStatus.equals("Pending Approval", ignoreCase = true)) {
                // Disable and gray out the "Extend Stay" button if the booking is pending approval
                binding.extendStayButton.isEnabled = false
                binding.extendStayButton.setBackgroundResource(R.drawable.filter_button_background_unselected) // Use a gray rounded background
            } else {
                // Enable the "Extend Stay" button for other statuses
                binding.extendStayButton.isEnabled = true
                binding.extendStayButton.setBackgroundResource(R.drawable.filter_button_selected) // Use the green rounded background
            }

            // Set click listeners for "Extend Stay" and "Cancel"
            binding.extendStayButton.setOnClickListener {
                if (binding.extendStayButton.isEnabled) { // Check if the button is enabled
                    extendStay() // Call Extend Stay functionality
                }
            }
            binding.cancelButton.setOnClickListener {
                cancelBooking() // Call Cancel Booking functionality
            }
        } else {
            // If accessed from HomeFragment
            binding.extendStayButton.visibility = View.GONE // Hide Extend Stay button
            binding.cancelButton.visibility = View.GONE // Hide Cancel button
            binding.bookButton.visibility = View.VISIBLE // Show the Book Now button

            binding.bookButton.setOnClickListener {
                // Navigate to BookingRoomActivity for booking
                navigateToBookingActivity()
            }
        }
    }


    private fun extendStay() {
        val room = intent.getParcelableExtra<Room>("room") ?: return
        val startDate = intent.getStringExtra("startDate")
        val endDate = intent.getStringExtra("endDate")

        val intent = Intent(this, BookingRoomActivity::class.java).apply {
            putExtra("roomTitle", room.title)
            putExtra("roomPrice", removeNightSuffix(room.price).toInt())
            putExtra("imageUrl", room.imageUrl)
            putExtra("startDate", startDate)
            putExtra("endDate", endDate)
            putExtra("isExtendable", true) // Set true when extending
        }
        startActivity(intent)

        startActivity(intent)
    }

    private fun cancelBooking() {
        val room = intent.getParcelableExtra<Room>("room") ?: return

        // Show confirmation dialog before cancelling
        showCancellationDialog(room.id)
    }
    private fun showCancellationDialog(roomId: String?, useBottomSheet: Boolean = true) {
        if (roomId == null) return

        if (useBottomSheet) {
            // Use BottomSheetDialogFragment
            val cancelBookingFragment = CancelBookingFragment.newInstance(roomId)
            cancelBookingFragment.show(supportFragmentManager, "CancelBookingFragment")
        } else {
            // Use AlertDialog
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes") { _, _ ->
                    // Perform cancellation logic
                    val databaseReference = FirebaseDatabase.getInstance().getReference("bookings")
                    databaseReference.child(roomId).removeValue() // Remove the booking from the database
                        .addOnSuccessListener {
                            showToast("Booking canceled successfully.")
                            finish() // Close the activity after cancellation
                        }
                        .addOnFailureListener { exception ->
                            // Handle failure case
                            showToast("Failed to cancel booking: ${exception.message}")
                        }
                }
                .setNegativeButton("No", null) // Do nothing on "No"
                .create()
            dialog.show()
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    // Function to navigate to BookingRoomActivity
    private fun navigateToBookingActivity() {
        val room = intent.getParcelableExtra<Room>("room") ?: return

        val intent = Intent(this, BookingRoomActivity::class.java).apply {
            putExtra("roomTitle", room.title) // Pass room title
            putExtra("roomPrice", removeNightSuffix(room.price).toInt()) // Pass room price as integer
            putExtra("roomType", room.title) // Pass the room type dynamically
            putExtra("paxCount", room.people?.toInt() ?: 2) // Pass the number of people (converted to Int)
            putExtra("imageUrl", room.imageUrl) // Pass the image URL
        }
        startActivity(intent)
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
