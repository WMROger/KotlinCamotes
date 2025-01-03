package com.example.kotlinactivities.homePage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.kotlinactivities.MainActivity
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.ImageCarouselAdapter
import com.example.kotlinactivities.databinding.ActivityRoomDetailsBinding
import com.example.kotlinactivities.model.Room
import com.example.kotlinactivities.myRoom.CancelBookingFragment
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.*

class RoomDetailsActivity : AppCompatActivity(), CancelBookingFragment.OnDismissListener {

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
            finish() // Handle error if room is not passed correctly
            return
        }

        // Populate Room Details
        populateRoomDetails(room)

        // Update the button text dynamically
        updateBookingButton(isFromMyRoom, bookingStatus, room)

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
        // Check if there are image URLs
        val images = room.imageUrls ?: listOf() // Default to an empty list if null

        if (images.isNotEmpty()) {
            // Multiple images: Set up the carousel
            val imageCarouselAdapter = ImageCarouselAdapter(images)
            binding.roomImage.adapter = imageCarouselAdapter // Bind adapter to ViewPager2

            // Setup dots indicator
            setupDots(images.size)

            // Register a callback to update dots on page change
            binding.roomImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateDots(position)
                }
            })
        } else {
            // Single image case: Use a placeholder or the first image URL directly
            binding.roomImage.visibility = View.VISIBLE
            val singleImageAdapter = ImageCarouselAdapter(listOf(room.imageUrl ?: R.drawable.ic_splash3.toString()))
            binding.roomImage.adapter = singleImageAdapter
        }

        // Populate other room details
        binding.roomTitle.text = room.title
        binding.roomLocation.text = "Himensulan Island, Camotes Cebu" // Static for now
        binding.roomRating.text = room.rating
        binding.roomPrice.text = formatPrice(removeNightSuffix(room.price)) // Format price
        binding.roomDescription.text =
            "Indulge in luxury and comfort in our ${room.title}, featuring elegant interiors, plush bedding, a spacious seating area, and modern amenities."
    }

    // Function to setup dots for the image carousel
    private fun setupDots(count: Int) {
        binding.indicatorLayout.removeAllViews() // Clear previous dots

        for (i in 0 until count) {
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    if (i == 0) 32 else 12,  // Wider and larger for active dots
                    12  // Keep the height the same for all dots
                ).apply {
                    marginEnd = 12 // Increase spacing between dots
                }
                setBackgroundResource(if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive)
            }
            binding.indicatorLayout.addView(dot)
        }
    }

    // Function to update dots based on the selected page
    private fun updateDots(position: Int) {
        for (i in 0 until binding.indicatorLayout.childCount) {
            val dot = binding.indicatorLayout.getChildAt(i)
            val layoutParams = dot.layoutParams as LinearLayout.LayoutParams
            layoutParams.width = if (i == position) 32 else 12 // Adjust width for active/inactive
            layoutParams.height = 12 // Keep height consistent
            dot.layoutParams = layoutParams
            dot.setBackgroundResource(if (i == position) R.drawable.dot_active else R.drawable.dot_inactive)
        }
    }

    // Function to update the booking button text and behavior
    private fun updateBookingButton(isFromMyRoom: Boolean, bookingStatus: String, room: Room) {
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
                    extendStay()
                }
            }
            binding.cancelButton.setOnClickListener {
                // Pass the room ID to showCancellationDialog
                showCancellationDialog(room.id ?: "")
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
            putExtra("imageUrl", room.imageUrls?.firstOrNull()) // Pass the first URL explicitly
            putExtra("startDate", startDate)
            putExtra("endDate", endDate)
            putExtra("isExtendable", true) // Set true when extending
        }
        startActivity(intent)
    }

    private fun showCancellationDialog(roomId: String) {
        val cancelBookingFragment = CancelBookingFragment.newInstance(roomId)
        cancelBookingFragment.show(supportFragmentManager, "CancelBookingFragment")
    }

    override fun onDialogDismissed() {
        // Perform the actual cancellation logic after the dialog is dismissed
        val roomId = intent.getParcelableExtra<Room>("room")?.id ?: return
        val databaseReference = FirebaseDatabase.getInstance().getReference("bookings")

        databaseReference.child(roomId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Booking canceled successfully.",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("navigateTo", "MyRoomFragment") // Indicate navigation to "My Room" fragment
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { error ->
                Log.e("RoomDetailsActivity", "Failed to cancel booking: ${error.message}")
                Toast.makeText(
                    this,
                    "Failed to cancel booking: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun navigateToBookingActivity() {
        val room = intent.getParcelableExtra<Room>("room") ?: return

        val intent = Intent(this, BookingRoomActivity::class.java).apply {
            putExtra("roomTitle", room.title)
            putExtra("roomPrice", removeNightSuffix(room.price).toInt())
            putExtra("imageUrl", room.imageUrls?.firstOrNull()) // Pass the first image URL
        }
        startActivity(intent)
    }

    private fun toggleFavorite() {
        isFavorited = !isFavorited // Toggle the state
        val heartDrawable = if (isFavorited) {
            R.drawable.ic_heart // Red heart drawable when favorited
        } else {
            R.drawable.ic_heart_black // Black heart drawable when not favorited
        }

        binding.heartButton.setImageDrawable(ContextCompat.getDrawable(this, heartDrawable))
    }

    private fun removeNightSuffix(price: String?): String {
        return price?.replace("/night", "")?.replace("₱", "")?.replace(",", "")?.trim() ?: "0"
    }

    private fun formatPrice(price: String): String {
        val priceValue = price.toIntOrNull() ?: 0
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.maximumFractionDigits = 0
        return "₱${formatter.format(priceValue)}"
    }
}
