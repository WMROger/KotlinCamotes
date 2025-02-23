package com.example.kotlinactivities.userPage.homePage

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.kotlinactivities.MainActivity
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.ImageCarouselAdapter
import com.example.kotlinactivities.adapter.ItineraryAdapter
import com.example.kotlinactivities.databinding.ActivityRoomDetailsBinding
import com.example.kotlinactivities.model.ItineraryItem
import com.example.kotlinactivities.model.Room
import com.example.kotlinactivities.userPage.myRoom.CalendarBottomSheet
import com.example.kotlinactivities.userPage.myRoom.CancelBookingFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
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


        val itineraryList = listOf(
            ItineraryItem(R.drawable.ic_cave, "Cave Exploration", "Explore stunning caves with skylights."),
            ItineraryItem(R.drawable.ic_dive, "Dive", "Dive into vibrant coral reefs."),
            ItineraryItem(R.drawable.ic_marine, "Diverse Marine Life", "Encounter schools of fish."),
            ItineraryItem(R.drawable.ic_moments, "Capture Moments", "Underwater photography sessions."),
            ItineraryItem(R.drawable.ic_cave2, "Cave Exploration", "Marvel at crystal-clear cave pools."),
            ItineraryItem(R.drawable.ic_cave2, "Cave Exploration", "Marvel at crystal-clear cave pools.")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.itineraryRecyclerView)
        val layoutManager = GridLayoutManager(this, 2) // 2 Columns
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position % 3 == 0) 2 else 1 // Every third item takes full width
            }
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = ItineraryAdapter(itineraryList)




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
        // Image carousel setup
        val images = room.imageUrls ?: listOf()
        if (images.isNotEmpty()) {
            val imageCarouselAdapter = ImageCarouselAdapter(images)
            binding.roomImage.adapter = imageCarouselAdapter
            setupDots(images.size)
            binding.roomImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateDots(position)
                }
            })
        } else {
            val singleImageAdapter = ImageCarouselAdapter(listOf(room.imageUrl ?: R.drawable.ic_splash3.toString()))
            binding.roomImage.adapter = singleImageAdapter
        }

        // Populate static room details
        binding.roomTitle.text = room.title
        binding.roomLocation.text = "Himensulan Island, Camotes Cebu" // Static for now
        binding.roomRating.text = room.rating
        binding.roomPrice.text = formatPrice(removeNightSuffix(room.price)) // Format price
        binding.roomDescription.text =
            "Indulge in luxury and comfort in our ${room.title}, featuring elegant interiors, plush bedding, a spacious seating area, and modern amenities."

        // Fetch category and amenities from Firebase
        fetchCategoryAndAmenities(room.id)
    }

    private fun fetchCategoryAndAmenities(roomId: String?) {
        if (roomId == null) {
            Toast.makeText(this, "Room ID not found.", Toast.LENGTH_SHORT).show()
            return
        }

        val roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId)

        roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    // Fetch and display amenities
                    val amenities = snapshot.child("amenities").getValue(object : GenericTypeIndicator<List<String>>() {})
                    displayAmenities(amenities ?: emptyList())

                    // Fetch and display description
                    val description = snapshot.child("description").getValue(String::class.java)
                    binding.roomDescription.text = description ?: "No description available."
                } else {
                    Toast.makeText(this@RoomDetailsActivity, "Room data not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RoomDetailsActivity", "Failed to fetch data: ${error.message}")
                Toast.makeText(this@RoomDetailsActivity, "Failed to fetch room details.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayAmenities(amenities: List<String>) {
        val constraintLayout = binding.amenitiesContainer
        val flow = binding.amenitiesFlow

        // Remove previous views except Flow (first child)
        constraintLayout.removeViews(1, constraintLayout.childCount - 1)

        val viewIds = mutableListOf<Int>()

        for (amenity in amenities) {
            val textView = layoutInflater.inflate(R.layout.item_amenity, constraintLayout, false) as TextView
            textView.id = View.generateViewId() // Generate unique ID
            textView.text = amenity // Set the text dynamically
            constraintLayout.addView(textView)
            viewIds.add(textView.id)
        }

        // Update Flow with dynamically created views
        flow.referencedIds = viewIds.toIntArray()
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

            when (bookingStatus.lowercase(Locale.ROOT)) {
                "pending approval" -> {
                    // Disable Extend Stay
                    binding.extendStayButton.isEnabled = false
                    binding.extendStayButton.isClickable = false
                    binding.extendStayButton.setBackgroundColor(Color.GRAY)
                    binding.extendStayButton.setTextColor(Color.DKGRAY)
                }
                "approved" -> {
                    // Enable Extend Stay
                    binding.extendStayButton.isEnabled = true
                    binding.extendStayButton.isClickable = true
                    binding.extendStayButton.setBackgroundResource(R.drawable.filter_button_selected)
                    binding.extendStayButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                "cancelled" -> {
                    // Hide both buttons or gray them out
                    binding.extendStayButton.isEnabled = false
                    binding.extendStayButton.isClickable = false
                    binding.cancelButton.isEnabled = false
                    binding.cancelButton.isClickable = false
                    binding.extendStayButton.setTextColor(Color.DKGRAY)
                    binding.cancelButton.setTextColor(Color.DKGRAY)
                    binding.bookingActionsLayout.visibility = View.GONE
                    val layoutParams = binding.nestedview.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.bottomMargin = 0 // Set bottom margin to 0
                    binding.nestedview.layoutParams = layoutParams


//                    binding.cancelButton.visibility = View.GONE
                }
            }

            // Set click listeners for "Extend Stay" and "Cancel"
            binding.extendStayButton.setOnClickListener {
                if (binding.extendStayButton.isEnabled) {
                    showCalendarBottomSheet(room)
                }
            }

            binding.cancelButton.setOnClickListener {
                showCancellationDialog(room.id ?: "")
            }
        } else {
            // If accessed from HomeFragment
            binding.extendStayButton.visibility = View.GONE
            binding.cancelButton.visibility = View.GONE
            binding.bookButton.visibility = View.VISIBLE

            binding.bookButton.setOnClickListener {
                navigateToBookingActivity()
            }
        }
    }


    private fun showCalendarBottomSheet(room: Room) {
        val calendarBottomSheet = CalendarBottomSheet()
        calendarBottomSheet.setOnExtendStayListener { selectedDate ->
            Toast.makeText(this, "Extend stay confirmed for $selectedDate", Toast.LENGTH_SHORT).show()

            // Perform any action (e.g., save new booking dates or extend the current booking)
            Log.d("RoomDetailsActivity", "Extended stay for room: ${room.title} on $selectedDate")
        }
        calendarBottomSheet.show(supportFragmentManager, "CalendarBottomSheet")
    }

    private fun showCancellationDialog(roomId: String) {
        val cancelBookingFragment = CancelBookingFragment.newInstance(roomId)
        cancelBookingFragment.show(supportFragmentManager, "CancelBookingFragment")
    }

    override fun onDialogDismissed() {
        val roomId = intent.getParcelableExtra<Room>("room")?.id ?: return
        val databaseReference = FirebaseDatabase.getInstance().getReference("bookings")

        databaseReference.child(roomId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Booking canceled successfully.", Toast.LENGTH_SHORT).show()

                // Disable buttons and change appearance
                binding.extendStayButton.isEnabled = false
                binding.cancelButton.isEnabled = false

                binding.extendStayButton.setBackgroundColor(Color.GRAY)
                binding.cancelButton.setBackgroundColor(Color.GRAY)

                binding.bookButton.visibility = View.VISIBLE // Show "Book Now" button

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("navigateTo", "MyRoomFragment")
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { error ->
                Log.e("RoomDetailsActivity", "Failed to cancel booking: ${error.message}")
                Toast.makeText(this, "Failed to cancel booking: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }



    private fun navigateToBookingActivity() {
        val room = intent.getParcelableExtra<Room>("room") ?: return

        val intent = Intent(this, BookingRoomActivity::class.java).apply {
            putExtra("roomTitle", room.title)
            putExtra("roomPrice", removeNightSuffix(room.price).toInt())
            putExtra("imageUrl", room.imageUrls?.firstOrNull() ?: room.imageUrl) // Pass the first image URL or fallback to default
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
