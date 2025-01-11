package com.example.kotlinactivities.userPage.myRoom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.BottomSheetCalendarAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class CalendarBottomSheet : BottomSheetDialogFragment() {

    private var onExtendStayListener: ((Date) -> Unit)? = null
    private var selectedDate: Date? = null
    private val today = Date()
    private lateinit var calendarAdapter: BottomSheetCalendarAdapter
    private val occupiedDates = mutableListOf<Date>()
    private var latestEndDate: Date? = null
    private lateinit var databaseReference: DatabaseReference
    private val calendar = Calendar.getInstance() // Calendar instance to handle month navigation

    fun setOnExtendStayListener(listener: (Date) -> Unit) {
        onExtendStayListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_calendar_dialog, container, false)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("bookings")

        // Setup month navigation and week labels
        setupMonthNavigation(view)

        // Setup Calendar RecyclerView
        val calendarRecyclerView = view.findViewById<RecyclerView>(R.id.calendarRecyclerView)
        calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)

        // Generate calendar dates
        val dates = generateCalendarDates()

        // Initialize calendarAdapter
        calendarAdapter = BottomSheetCalendarAdapter(
            dates = dates,
            selectedDate = selectedDate,
            occupiedDates = occupiedDates,
            latestEndDate = latestEndDate,
            onDateClick = { date ->
                if (!occupiedDates.contains(date) && date.after(today) && (latestEndDate == null || date.after(latestEndDate))) {
                    selectedDate = date
                    updateSelectedDateText(view, date)
                    calendarAdapter.updateSelectedDate(selectedDate)
                }
            }
        )

        calendarRecyclerView.adapter = calendarAdapter

        // Fetch occupied dates from Firebase
        fetchOccupiedDates {
            calendarAdapter.notifyDataSetChanged()
        }

        // Handle Extend Stay Button
        val extendStayButton = view.findViewById<Button>(R.id.extendStayButton)
        extendStayButton.setOnClickListener {
            selectedDate?.let {
                onExtendStayListener?.invoke(it)
            }
            dismiss()
        }

        return view
    }

    private fun setupMonthNavigation(view: View) {
        val monthYearTextView = view.findViewById<TextView>(R.id.monthYearText)
        val prevMonthButton = view.findViewById<ImageView>(R.id.previousMonthButton)
        val nextMonthButton = view.findViewById<ImageView>(R.id.nextMonthButton)

        // Update the displayed month and year
        fun updateMonthYear() {
            val dateFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            monthYearTextView.text = dateFormatter.format(calendar.time)
        }

        // Set up the initial month and year
        updateMonthYear()

        // Handle previous month button
        prevMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            refreshCalendar()
            updateMonthYear()
        }

        // Handle next month button
        nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            refreshCalendar()
            updateMonthYear()
        }

        // Set up week labels (Mon-Sun)
        val weekLabels = listOf(
            view.findViewById<TextView>(R.id.weekLabelSun),
            view.findViewById<TextView>(R.id.weekLabelMon),
            view.findViewById<TextView>(R.id.weekLabelTue),
            view.findViewById<TextView>(R.id.weekLabelWed),
            view.findViewById<TextView>(R.id.weekLabelThu),
            view.findViewById<TextView>(R.id.weekLabelFri),
            view.findViewById<TextView>(R.id.weekLabelSat)
        )
        val weekDayNames = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
        weekLabels.forEachIndexed { index, label ->
            label.text = weekDayNames[index]
        }
    }

    private fun fetchOccupiedDates(onComplete: () -> Unit) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                occupiedDates.clear()
                latestEndDate = null

                for (bookingSnapshot in snapshot.children) {
                    val startDateMillis = bookingSnapshot.child("startDate").getValue(Long::class.java)
                    val endDateMillis = bookingSnapshot.child("endDate").getValue(Long::class.java)

                    if (startDateMillis != null && endDateMillis != null) {
                        val startDate = Date(startDateMillis)
                        val endDate = Date(endDateMillis)

                        if (latestEndDate == null || endDate.after(latestEndDate)) {
                            latestEndDate = endDate
                        }

                        val calendar = Calendar.getInstance()
                        calendar.time = startDate
                        while (!calendar.time.after(endDate)) {
                            occupiedDates.add(calendar.time)
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                }
                onComplete()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun updateSelectedDateText(view: View, date: Date) {
        val selectedDateTextView = view.findViewById<TextView>(R.id.selectedDateTextView)
        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        selectedDateTextView.text = "Selected Date: ${dateFormatter.format(date)}"
    }

    private fun refreshCalendar() {
        val dates = generateCalendarDates()
        calendarAdapter.updateDates(dates)
    }

    private fun generateCalendarDates(): List<Date> {
        val calendar = this.calendar.clone() as Calendar
        val dates = mutableListOf<Date>()

        // Set to the first day of the current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        calendar.add(Calendar.DAY_OF_MONTH, -startDayOfWeek)

        // Generate 35 days (5 rows × 7 columns) for the calendar grid
        for (i in 0 until 35) {
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dates
    }
}
