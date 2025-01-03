package com.example.kotlinactivities.myRoom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
    private var selectedDate: Date? = null // Keep track of the selected date
    private val today = Date()
    private lateinit var calendarAdapter: BottomSheetCalendarAdapter
    private val occupiedDates = mutableListOf<Date>() // List to hold occupied dates from Firebase
    private var latestEndDate: Date? = null // Store the latest end date
    private lateinit var databaseReference: DatabaseReference // Firebase Database reference

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

        // Generate calendar dates
        val dates = generateCalendarDates()

        // Setup Calendar RecyclerView
        val calendarRecyclerView = view.findViewById<RecyclerView>(R.id.calendarRecyclerView)
        calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)

        // Initialize calendarAdapter
        calendarAdapter = BottomSheetCalendarAdapter(
            dates = dates,
            selectedDate = selectedDate,
            occupiedDates = occupiedDates, // Pass the occupied dates
            latestEndDate = latestEndDate, // Pass the latest end date
            onDateClick = { date ->
                if (!occupiedDates.contains(date) && date.after(today) && (latestEndDate == null || date.after(latestEndDate))) {
                    selectedDate = date
                    updateSelectedDateText(view, date)
                    calendarAdapter.updateSelectedDate(selectedDate) // Update the selected date
                }
            }
        )

        calendarRecyclerView.adapter = calendarAdapter

        // Fetch occupied dates from Firebase
        fetchOccupiedDates {
            calendarAdapter.notifyDataSetChanged() // Refresh the adapter after fetching data
        }

        // Handle Extend Stay Button
        val extendStayButton = view.findViewById<Button>(R.id.extendStayButton)
        extendStayButton.setOnClickListener {
            selectedDate?.let {
                onExtendStayListener?.invoke(it) // Pass the selected date
            }
            dismiss() // Close the bottom sheet
        }

        return view
    }

    private fun fetchOccupiedDates(onComplete: () -> Unit) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                occupiedDates.clear() // Clear the list before adding new data
                latestEndDate = null // Reset the latest end date

                for (bookingSnapshot in snapshot.children) {
                    val startDateMillis = bookingSnapshot.child("startDate").getValue(Long::class.java)
                    val endDateMillis = bookingSnapshot.child("endDate").getValue(Long::class.java)

                    if (startDateMillis != null && endDateMillis != null) {
                        val startDate = Date(startDateMillis)
                        val endDate = Date(endDateMillis)

                        // Update the latest end date to the most recent one
                        if (latestEndDate == null || endDate.after(latestEndDate)) {
                            latestEndDate = endDate
                        }

                        // Add all dates in the range to the occupiedDates list
                        val calendar = Calendar.getInstance()
                        calendar.time = startDate
                        while (!calendar.time.after(endDate)) {
                            occupiedDates.add(calendar.time)
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                }
                onComplete() // Notify that the data fetching is complete
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

    private fun generateCalendarDates(): List<Date> {
        val calendar = Calendar.getInstance()
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
