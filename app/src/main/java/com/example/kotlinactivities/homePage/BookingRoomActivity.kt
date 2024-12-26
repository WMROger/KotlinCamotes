package com.example.kotlinactivities.homePage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.CalendarAdapter
import java.text.SimpleDateFormat
import java.util.*

class BookingRoomActivity : AppCompatActivity() {

    private lateinit var monthYearText: TextView
    private lateinit var selectedDateRange: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var prevMonthButton: Button
    private lateinit var nextMonthButton: Button
    private lateinit var backButton: ImageView
    private lateinit var bookingSubtitle: TextView

    private val calendar = Calendar.getInstance()
    private val today = Date()
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val rangeFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_room)

        // Initialize views
        monthYearText = findViewById(R.id.monthYearText)
        selectedDateRange = findViewById(R.id.selectedDateRange)
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        prevMonthButton = findViewById(R.id.previousMonthButton)
        nextMonthButton = findViewById(R.id.nextMonthButton)
        backButton = findViewById(R.id.backButton)
        bookingSubtitle = findViewById(R.id.bookingSubtitle)
        val guestCountText = findViewById<TextView>(R.id.guestCount)
        val minusButton = findViewById<Button>(R.id.minusButton)
        val plusButton = findViewById<Button>(R.id.plusButton)

        // Retrieve data from the previous activity
        val roomType = intent.getStringExtra("roomType") ?: "Regular Room"
        val paxCount = intent.getIntExtra("paxCount", 2)
        bookingSubtitle.text = "$roomType | $paxCount pax"

        // Set initial guest count
        var guestCount = paxCount
        guestCountText.text = guestCount.toString()

        // Set up guest count buttons
        minusButton.setOnClickListener {
            if (guestCount > 1) {
                guestCount--
                guestCountText.text = guestCount.toString()
            }
        }

        plusButton.setOnClickListener {
            guestCount++
            guestCountText.text = guestCount.toString()
        }

        // Set back button functionality
        backButton.setOnClickListener {
            finish() // Close this activity and return to the previous one
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
            selectedDateRange.text = "From: ${rangeFormatter.format(startDate!!)}\nTo: ${rangeFormatter.format(endDate!!)}"
        } else {
            selectedDateRange.text = "Start Date: ${rangeFormatter.format(startDate!!)}"
        }

        // Update the adapter with the selected range
        (calendarRecyclerView.adapter as CalendarAdapter).updateSelectedRange(startDate, endDate)
    }
}
