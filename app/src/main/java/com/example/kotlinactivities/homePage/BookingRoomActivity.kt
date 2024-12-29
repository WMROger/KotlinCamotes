package com.example.kotlinactivities.homePage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.MainActivity
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.CalendarAdapter
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.database.FirebaseDatabase

class BookingRoomActivity : AppCompatActivity() {

    private lateinit var monthYearText: TextView
    private lateinit var selectedDateRange: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var prevMonthButton: Button
    private lateinit var nextMonthButton: Button
    private lateinit var totalPriceTextView: TextView
    private lateinit var backButton: ImageView
    private lateinit var guestCountText: TextView
    private lateinit var plusButton: Button
    private lateinit var minusButton: Button

    private val calendar = Calendar.getInstance()
    private val today = Date()
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val rangeFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    private var startDate: Date? = null
    private var endDate: Date? = null
    private var roomPrice: Int = 0 // Room price as an integer
    private var guestCount: Int = 1 // Default guest count
    private var totalPrice: Int = 0 // Total price for the booking
    // Change `imageUrl` from Int to String
    private var imageUrl: String = "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png"

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_room)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize views
        monthYearText = findViewById(R.id.monthYearText)
        selectedDateRange = findViewById(R.id.selectedDateRange)
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        prevMonthButton = findViewById(R.id.previousMonthButton)
        nextMonthButton = findViewById(R.id.nextMonthButton)
        totalPriceTextView = findViewById(R.id.roomPrice)
        backButton = findViewById(R.id.backButton)
        guestCountText = findViewById(R.id.guestCount)
        plusButton = findViewById(R.id.plusButton)
        minusButton = findViewById(R.id.minusButton)
        val rbGcash = findViewById<RadioButton>(R.id.rb_gcash)
        val rbCash = findViewById<RadioButton>(R.id.rb_cash)
        val bookNowButton = findViewById<Button>(R.id.bookNowButton)

        // Retrieve data from the previous activity
        val roomTitle = intent.getStringExtra("roomTitle") ?: "Room"
        val priceValue = intent.getIntExtra("roomPrice", 0)
        roomPrice = priceValue // Use parsed integer price
        imageUrl = intent.getStringExtra("imageUrl").toString() // Retrieve image URL

        // Retrieve logged-in user details
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email ?: "Unknown User" // Get user email
        val userId = currentUser?.uid ?: "Unknown ID" // Get user UID

        // Set back button functionality
        backButton.setOnClickListener {
            finish() // Close this activity and return to the previous one
        }

        bookNowButton.setOnClickListener {
            when {
                rbGcash.isChecked -> {
                    // Validate that dates are selected
                    if (startDate == null || endDate == null) {
                        Toast.makeText(
                            this,
                            "Please select a valid date range before proceeding.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }

                    // Redirect to PaymentNotImplementedActivity for GCash
                    val intent = Intent(this, PaymentNotImplementedActivity::class.java)
                    intent.putExtra("totalPrice", totalPrice * 100) // Pass price in centavos
                    intent.putExtra("roomTitle", roomTitle) // Pass room title
                    intent.putExtra("guestCount", guestCount) // Pass the updated guest count
                    intent.putExtra("roomPrice", roomPrice) // Pass the room price
                    intent.putExtra("userEmail", userEmail) // Pass user email
                    intent.putExtra("userId", userId) // Pass user ID
                    intent.putExtra("imageUrl", imageUrl) // Pass the image URL
                    intent.putExtra("startDate", startDate?.time) // Pass start date as timestamp
                    intent.putExtra("endDate", endDate?.time) // Pass end date as timestamp
                    intent.putExtra("totalDays", calculateTotalDays()) // Pass the total number of days
                    intent.putExtra("paymentMethod", "Gcash") // Include payment method
                    intent.putExtra("paymentStatus", "Pending") // Default status for Gcash
                    startActivity(intent)
                }

                rbCash.isChecked -> {
                    // Validate that dates are selected
                    if (startDate == null || endDate == null) {
                        Toast.makeText(
                            this,
                            "Please select a valid date range before proceeding.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }

                    // Recalculate the total price before uploading
                    updateTotalPrice()

                    // Check if totalPrice is valid
                    if (totalPrice <= 0) {
                        Toast.makeText(
                            this,
                            "Error: Total price cannot be zero. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }

                    // Upload booking details to Firebase for cash payment
                    val database = FirebaseDatabase.getInstance()
                    val bookingsRef = database.getReference("bookings")

                    // Create a unique booking ID
                    val bookingId = bookingsRef.push().key

                    if (bookingId != null) {
                        // Create booking data to upload
                        val bookingData = mapOf(
                            "userId" to userId,
                            "userEmail" to userEmail,
                            "roomTitle" to roomTitle,
                            "roomPrice" to roomPrice,
                            "guestCount" to guestCount,
                            "totalPrice" to totalPrice, // Ensure this is updated
                            "totalDays" to calculateTotalDays(),
                            "startDate" to startDate?.time, // Store as timestamp
                            "endDate" to endDate?.time,     // Store as timestamp
                            "imageUrl" to imageUrl,         // Pass the image URL
                            "paymentMethod" to "Cash",      // Include payment method
                            "paymentStatus" to "Pending Approval" // Default status for cash payment
                        )

                        // Upload booking data to Firebase
                        bookingsRef.child(bookingId).setValue(bookingData)
                            .addOnSuccessListener {
                                // Navigate to HomeFragment on success
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("navigateTo", "HomeFragment")
                                startActivity(intent)

                                // Show a confirmation toast
                                Toast.makeText(
                                    this,
                                    "Booking submitted. Please wait for approval.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener { error ->
                                // Show an error message if the upload fails
                                Toast.makeText(
                                    this,
                                    "Failed to submit booking: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        // Show an error if booking ID couldn't be generated
                        Toast.makeText(
                            this,
                            "Failed to generate booking ID. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                else -> {
                    // Show an error if no payment method is selected
                    Toast.makeText(
                        this,
                        "Please select a payment method to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Initialize RecyclerView layout manager
        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)

        // Set button listeners
        prevMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
        rbGcash.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rbCash.isChecked = false
            }
        }

        rbCash.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rbGcash.isChecked = false
            }
        }
        // Initialize plus and minus button functionality
        plusButton.setOnClickListener {
            if (guestCount < 10) { // Limit maximum guests to 10
                guestCount++
                updateGuestCount()
            }
        }
        minusButton.setOnClickListener {
            if (guestCount > 1) { // Minimum guests should be 1
                guestCount--
                updateGuestCount()
            }
        }

        // Initialize the calendar
        updateCalendar()
    }

    private fun calculateTotalDays(): Int {
        return if (startDate != null && endDate != null) {
            val diffInMillis = endDate!!.time - startDate!!.time
            (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1 // Include the start day
        } else {
            1 // Default to 1 day if no range is selected
        }
    }

    private fun updateCalendar() {
        // Set the current month and year
        monthYearText.text = dateFormat.format(calendar.time)

        // Generate the dates for the current month
        val dates = generateCalendarDates()

        // Update the adapter
        val calendarAdapter = CalendarAdapter(dates, today, startDate, endDate) { date ->
            handleDateSelection(date)
        }
        calendarRecyclerView.adapter = calendarAdapter
    }

    private fun generateCalendarDates(): List<Date> {
        val dates = mutableListOf<Date>()
        val calendarStart = calendar.clone() as Calendar
        calendarStart.set(Calendar.DAY_OF_MONTH, 1)

        // Move to the start of the week
        val firstDayOfWeek = calendarStart.get(Calendar.DAY_OF_WEEK) - 1
        calendarStart.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek)

        // Add 4 weeks of dates (7 * 4 = 28 days)
        for (i in 0 until 35) {
            dates.add(calendarStart.time)
            calendarStart.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dates
    }

    private fun handleDateSelection(date: Date) {
        if (startDate == null || endDate != null) {
            // Reset selection
            startDate = date
            endDate = null
        } else {
            // Set end date
            if (date.before(startDate)) {
                endDate = startDate
                startDate = date
            } else {
                endDate = date
            }
        }

        // Update the selected date range text
        if (startDate != null && endDate != null) {
            selectedDateRange.text =
                "From: ${rangeFormatter.format(startDate!!)}\nTo: ${rangeFormatter.format(endDate!!)}"
            updateTotalPrice()
        } else {
            selectedDateRange.text = "Start Date: ${rangeFormatter.format(startDate!!)}"
            updateTotalPrice(singleDay = true)
        }

        // Update the adapter with the selected range
        (calendarRecyclerView.adapter as CalendarAdapter).updateSelectedRange(startDate, endDate)
    }

    private fun updateTotalPrice(singleDay: Boolean = false) {
        val totalDays = if (singleDay || endDate == null) {
            1
        } else {
            val diffInMillis = endDate!!.time - startDate!!.time
            (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1 // Include the start day
        }

        totalPrice = roomPrice * totalDays // Calculate total price based on room price per night and total days
        totalPriceTextView.text = formatPrice(totalPrice) // Display the total price
    }

    private fun updateGuestCount() {
        guestCountText.text = "$guestCount" // Display the guest count
    }

    private fun formatPrice(price: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.maximumFractionDigits = 0
        return "₱${formatter.format(price)}"
    }
}
