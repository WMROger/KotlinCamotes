package com.example.kotlinactivities.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(
    private var dates: List<Date>,
    private val today: Date,
    private var startDate: Date?,
    private var endDate: Date?,
    private val onDateClick: (Date) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val dateFormat = SimpleDateFormat("d", Locale.getDefault())

    fun updateSelectedRange(start: Date?, end: Date?) {
        startDate = start
        endDate = end
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_date, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = dates[position]
        holder.bind(date)
    }

    override fun getItemCount(): Int = dates.size

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.dateText)

        fun bind(date: Date) {
            dateText.text = dateFormat.format(date)

            // Reset styles
            dateText.setBackgroundColor(Color.TRANSPARENT)
            dateText.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))

            // Highlight today's date
            if (isSameDay(date, today)) {
                dateText.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
            }

            // Use local copies of startDate and endDate
            val localStartDate = startDate
            val localEndDate = endDate

            // Highlight the selected range
            if (localStartDate != null && localEndDate != null && date in localStartDate..localEndDate) {
                when {
                    isSameDay(date, localStartDate) || isSameDay(date, localEndDate) -> {
                        dateText.setBackgroundResource(R.drawable.bg_date)
                    }
                    else -> {
                        dateText.setBackgroundResource(R.drawable.bg_date_range)
                    }
                }
                dateText.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            }

            // Disable past dates
            if (date.before(today)) {
                dateText.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray))
                itemView.isEnabled = false
            } else {
                itemView.isEnabled = true
            }

            // Handle click events
            itemView.setOnClickListener {
                onDateClick(date)
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
