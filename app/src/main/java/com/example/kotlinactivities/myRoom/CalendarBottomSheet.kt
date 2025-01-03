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
import java.text.SimpleDateFormat
import java.util.*

class CalendarBottomSheet : BottomSheetDialogFragment() {

    private var onExtendStayListener: ((Date) -> Unit)? = null
    private var selectedDate: Date? = null
    private val today = Date()
    private lateinit var calendarAdapter: BottomSheetCalendarAdapter // Declare at the class level

    fun setOnExtendStayListener(listener: (Date) -> Unit) {
        onExtendStayListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_calendar_dialog, container, false)

        // Generate calendar dates
        val dates = generateCalendarDates()

        // Setup Calendar RecyclerView
        val calendarRecyclerView = view.findViewById<RecyclerView>(R.id.calendarRecyclerView)
        calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)

        // Initialize calendarAdapter
        calendarAdapter = BottomSheetCalendarAdapter(
            dates = dates,
            selectedDate = selectedDate,
            onDateClick = { date ->
                selectedDate = date
                updateSelectedDateText(view, date)
                calendarAdapter.notifyDataSetChanged() // Refresh UI
            }
        )

        calendarRecyclerView.adapter = calendarAdapter

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

        // Generate 42 days (6 weeks) for the calendar grid
        for (i in 0 until 35) {
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dates
    }
}
