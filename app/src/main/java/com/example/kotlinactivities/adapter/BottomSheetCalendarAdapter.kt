package com.example.kotlinactivities.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import java.text.SimpleDateFormat
import java.util.*

class BottomSheetCalendarAdapter(
    private val dates: List<Date>, // List of dates for the calendar
    private var selectedDate: Date?, // The currently selected date
    private val occupiedDates: List<Date>, // The list of occupied dates
    private val latestEndDate: Date?, // The latest end date from Firebase
    private val onDateClick: (Date) -> Unit // Callback when a date is clicked
) : RecyclerView.Adapter<BottomSheetCalendarAdapter.CalendarViewHolder>() {

    private val dateFormat = SimpleDateFormat("d", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_date, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = dates[position]
        holder.bind(date)
    }

    override fun getItemCount(): Int = dates.size

    fun updateSelectedDate(newSelectedDate: Date?) {
        selectedDate = newSelectedDate
        notifyDataSetChanged() // Refresh the calendar
    }

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.dateText)

        fun bind(date: Date) {
            dateText.text = dateFormat.format(date)

            // Reset styles
            dateText.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
            dateText.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))

            // Highlight the selected date
            if (selectedDate != null && isSameDay(date, selectedDate!!)) {
                dateText.setBackgroundResource(R.drawable.filter_button_selected) // Highlight selected date
                dateText.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            }

            // Disable and highlight occupied dates
            if (occupiedDates.any { isSameDay(it, date) }) {
                dateText.setBackgroundResource(R.drawable.bg_date_occupied) // Occupied date background
                dateText.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray))
                itemView.isEnabled = false
                return
            }

            // Disable dates before or equal to latestEndDate
            if (latestEndDate != null && !date.after(latestEndDate)) {
                dateText.setBackgroundResource(R.drawable.bg_date_range) // Disabled date background
                dateText.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray))
                itemView.isEnabled = false
                return
            }

            // Disable past dates
            val today = Date()
            if (date.before(today)) {
                dateText.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray))
                itemView.isEnabled = false
            } else {
                itemView.isEnabled = true
            }

            // Handle click events
            itemView.setOnClickListener {
                if (!date.before(today) && !occupiedDates.contains(date)) {
                    onDateClick(date)
                }
            }
        }

        private fun isSameDay(date1: Date, date2: Date): Boolean {
            val calendar1 = Calendar.getInstance()
            val calendar2 = Calendar.getInstance()
            calendar1.time = date1
            calendar2.time = date2
            return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                    calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
        }
    }
}
