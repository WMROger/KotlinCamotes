package com.example.kotlinactivities.homePage

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinactivities.R
import java.text.SimpleDateFormat
import java.util.*

class BookingRoomActivity : AppCompatActivity() {

    private var guestCount = 2 // Initial guest count
    private var startDate: Long? = null // Start date
    private var endDate: Long? = null // End date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_room)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val selectedDateRange = findViewById<TextView>(R.id.selectedDateRange)
        val minusButton = findViewById<Button>(R.id.minusButton)
        val plusButton = findViewById<Button>(R.id.plusButton)
        val guestCountText = findViewById<TextView>(R.id.guestCount)
        val backButton = findViewById<ImageView>(R.id.backButton)
        // Initialize guest count
        guestCountText.text = guestCount.toString()

        // Block past dates in CalendarView
        calendarView.minDate = System.currentTimeMillis()

        backButton.setOnClickListener{
            finish()
        }
        // Handle CalendarView date selection
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            val selectedDate = selectedCalendar.timeInMillis

            if (startDate == null || (startDate != null && endDate != null)) {
                // Set the start date
                startDate = selectedDate
                endDate = null
                selectedDateRange.text = "Start Date: ${formatDate(selectedDate)}"
            } else {
                // Set the end date
                endDate = selectedDate
                if (endDate!! < startDate!!) {
                    // If the end date is earlier than the start date, swap them
                    val temp = startDate
                    startDate = endDate
                    endDate = temp
                }
                selectedDateRange.text =
                    "From: ${formatDate(startDate!!)}\nTo: ${formatDate(endDate!!)}"
            }

            // Highlight the selected date range
            highlightDateRange(calendarView, startDate, endDate)
        }

        // Handle minus button click
        minusButton.setOnClickListener {
            if (guestCount > 1) {
                guestCount--
                guestCountText.text = guestCount.toString()
            } else {
                Toast.makeText(this, "Guests cannot be less than 1", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle plus button click
        plusButton.setOnClickListener {
            guestCount++
            guestCountText.text = guestCount.toString()
        }
    }

    private fun formatDate(dateInMillis: Long): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(dateInMillis))
    }

    private fun highlightDateRange(
        calendarView: CalendarView,
        startDate: Long?,
        endDate: Long?
    ) {
        // Refresh the CalendarView with custom decorations if needed (This is just conceptual)
        if (startDate != null && endDate != null) {
            // Ideally, you would implement a custom view or third-party library like `MaterialCalendarView`
            // to visually highlight date ranges. Native CalendarView does not support direct customization.
            Toast.makeText(
                this,
                "Date Range Highlighted:\n${formatDate(startDate)} - ${formatDate(endDate)}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
