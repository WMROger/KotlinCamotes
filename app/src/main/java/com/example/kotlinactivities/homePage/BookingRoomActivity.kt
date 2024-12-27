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
import com.example.kotlinactivities.navBar.PaymentNotImplementedActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_room)

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

        // Set back button functionality
        backButton.setOnClickListener {
            finish() // Close this activity and return to the previous one
        }

        bookNowButton.setOnClickListener {
            when {
                rbGcash.isChecked -> {
                    // Redirect to paymentNotImplementedActivity
                    val intent = Intent(this, PaymentNotImplementedActivity::class.java)
                    intent.putExtra("totalPrice", totalPrice * 100) // Pass price in centavos
                    startActivity(intent)
                }

                rbCash.isChecked -> {
                    // Redirect to HomeFragment with a toast
                    val intent = Intent(this, MainActivity::class.java) // Assuming MainActivity hosts HomeFragment
                    intent.putExtra("navigateTo", "HomeFragment") // Pass data to navigate to the fragment
                    startActivity(intent)

                    // Show a toast message
                    Toast.makeText(
                        this,
                        "Booking submitted. Please wait for approval.",
                        Toast.LENGTH_LONG
                    ).show()
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

        totalPrice = roomPrice * totalDays // Update the total price
        totalPriceTextView.text = formatPrice(totalPrice)
    }

    private fun updateGuestCount() {
        guestCountText.text = guestCount.toString()
    }

    private fun formatPrice(price: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.maximumFractionDigits = 0
        return "₱${formatter.format(price)}"
    }
}
