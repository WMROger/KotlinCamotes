package com.example.kotlinactivities.userPage.homePage

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

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
    private var imageUrl: String = "https://waveaway.scarlet2.io/assets/default_image.png" // Default image URL

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
        roomPrice = priceValue
        imageUrl = intent.getStringExtra("imageUrl") ?: imageUrl // Retrieve the passed first image URL or fallback to default


        // Retrieve logged-in user details
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email ?: "Unknown User"
        val userId = currentUser?.uid ?: "Unknown ID"

        // Set back button functionality
        backButton.setOnClickListener {
            finish() // Close this activity and return to the previous one
        }

        bookNowButton.setOnClickListener {
            // Validate that dates are selected
            if (startDate == null || endDate == null) {
                Toast.makeText(
                    this,
                    "Please select a valid date range before proceeding.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Recalculate the total price before processing
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

            // Handle the selected payment method
            when {
                rbGcash.isChecked -> {
                    handleGcashBooking(roomTitle, totalPrice)
                }

                rbCash.isChecked -> {
                    uploadBookingToFirebase(
                        roomTitle = roomTitle,
                        userId = userId,
                        userEmail = userEmail,
                        firstImageUrl = imageUrl,
                        paymentMethod = "Cash"
                    )
                }

                else -> {
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
        setupButtonListeners(rbGcash, rbCash)
        setupGuestCountListeners()
        updateCalendar()
    }

    private fun handleGcashBooking(roomTitle: String, totalPrice: Int) {
        val intent = Intent(this, PaymentNotImplementedActivity::class.java)
        intent.putExtra("roomTitle", roomTitle)
        intent.putExtra("totalPrice", totalPrice * 100) // Convert to centavos
        intent.putExtra("roomPrice", roomPrice)
        intent.putExtra("guestCount", guestCount)
        intent.putExtra("imageUrl", imageUrl)
        intent.putExtra("startDate", startDate?.time) // Pass start date as timestamp
        intent.putExtra("endDate", endDate?.time) // Pass end date as timestamp
        intent.putExtra("userId", firebaseAuth.currentUser?.uid ?: "Unknown ID")
        intent.putExtra("userEmail", firebaseAuth.currentUser?.email ?: "Unknown User")
        intent.putExtra("paymentMethod", "Gcash")
        startActivity(intent)
    }



    private fun uploadBookingToFirebase(
        roomTitle: String,
        userId: String,
        userEmail: String,
        firstImageUrl: String,
        paymentMethod: String // Add payment method parameter
    ) {
        val database = FirebaseDatabase.getInstance()
        val bookingsRef = database.getReference("bookings")

        // Create a unique booking ID
        val bookingId = bookingsRef.push().key

        if (bookingId != null && startDate != null && endDate != null) { // Ensure dates are not null
            val bookingData = mapOf(
                "userId" to userId,
                "userEmail" to userEmail,
                "roomTitle" to roomTitle,
                "roomPrice" to roomPrice,
                "guestCount" to guestCount,
                "totalPrice" to totalPrice,
                "totalDays" to calculateTotalDays(),
                "startDate" to startDate!!.time, // Ensure dates are not null
                "endDate" to endDate!!.time,
                "imageUrl" to firstImageUrl,
                "paymentMethod" to paymentMethod, // Use the passed payment method
                "paymentStatus" to "Pending Approval"
            )

            // Upload booking data to Firebase
            bookingsRef.child(bookingId).setValue(bookingData)
                .addOnSuccessListener {
                    Log.d("BookingRoomActivity", "Booking uploaded successfully.")
                    Toast.makeText(
                        this,
                        "Booking submitted. Please wait for approval.",
                        Toast.LENGTH_LONG
                    ).show()
                    navigateToHomeFragment()
                }
                .addOnFailureListener { error ->
                    Log.e("BookingRoomActivity", "Failed to upload booking: ${error.message}")
                    Toast.makeText(
                        this,
                        "Failed to submit booking: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            Toast.makeText(
                this,
                "Failed to upload booking. Ensure all fields are selected.",
                Toast.LENGTH_LONG
            ).show()
        }
    }




    private fun navigateToHomeFragment() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("navigateTo", "HomeFragment")
        startActivity(intent)
    }

    private fun setupButtonListeners(rbGcash: RadioButton, rbCash: RadioButton) {
        prevMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        rbGcash.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) rbCash.isChecked = false
        }

        rbCash.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) rbGcash.isChecked = false
        }
    }

    private fun setupGuestCountListeners() {
        plusButton.setOnClickListener {
            if (guestCount < 10) {
                guestCount++
                updateGuestCount()
            }
        }
        minusButton.setOnClickListener {
            if (guestCount > 1) {
                guestCount--
                updateGuestCount()
            }
        }
    }

    private fun calculateTotalDays(): Int {
        return if (startDate != null && endDate != null) {
            val diffInMillis = endDate!!.time - startDate!!.time
            (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1 // Include the start day
        } else {
            1
        }
    }

    private fun updateCalendar() {
        monthYearText.text = dateFormat.format(calendar.time)
        val dates = generateCalendarDates()
        val calendarAdapter = CalendarAdapter(dates, today, startDate, endDate) { date ->
            handleDateSelection(date)
        }
        calendarRecyclerView.adapter = calendarAdapter
    }

    private fun generateCalendarDates(): List<Date> {
        val dates = mutableListOf<Date>()
        val calendarStart = calendar.clone() as Calendar
        calendarStart.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = calendarStart.get(Calendar.DAY_OF_WEEK) - 1
        calendarStart.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek)

        for (i in 0 until 35) {
            dates.add(calendarStart.time)
            calendarStart.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dates
    }

    private fun handleDateSelection(date: Date) {
        if (startDate == null || endDate != null) {
            // If no dates are selected or both are selected, reset the range
            startDate = date
            endDate = null
        } else {
            // If startDate is already selected, determine the range
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
        } else if (startDate != null) {
            selectedDateRange.text = "Start Date: ${rangeFormatter.format(startDate!!)}"
        }

        // Notify the adapter to refresh the calendar selection
        (calendarRecyclerView.adapter as CalendarAdapter).updateSelectedRange(startDate, endDate)
    }


    private fun updateTotalPrice(singleDay: Boolean = false) {
        val totalDays = if (singleDay || endDate == null) {
            1
        } else {
            val diffInMillis = endDate!!.time - startDate!!.time
            (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
        }

        totalPrice = roomPrice * totalDays
        totalPriceTextView.text = formatPrice(totalPrice)
    }

    private fun updateGuestCount() {
        guestCountText.text = "$guestCount"
    }

    private fun formatPrice(price: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.maximumFractionDigits = 0
        return "₱${formatter.format(price)}"
    }
}
