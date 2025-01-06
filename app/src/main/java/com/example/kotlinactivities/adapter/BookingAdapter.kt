package com.example.kotlinactivities.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.model.Booking
import java.text.SimpleDateFormat
import java.util.*

class BookingAdapter(private val bookings: List<Booking>) :
    RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.bind(booking)
    }

    override fun getItemCount(): Int = bookings.size

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameText: TextView = itemView.findViewById(R.id.userName)
        private val reservationDateText: TextView = itemView.findViewById(R.id.reservationDate)
        private val checkInText: TextView = itemView.findViewById(R.id.checkIn)
        private val checkOutText: TextView = itemView.findViewById(R.id.checkOut)
        private val roomTypeText: TextView = itemView.findViewById(R.id.roomType)
        private val paymentStatusText: TextView = itemView.findViewById(R.id.paymentStatus)
        private val totalPriceText: TextView = itemView.findViewById(R.id.totalPrice)
        private val paidButton: Button = itemView.findViewById(R.id.paidButton)
        private val cancelButton: Button = itemView.findViewById(R.id.cancelButton)

        fun bind(booking: Booking) {
            // Set user email as the user name
            userNameText.text = booking.userEmail

            // Format reservation date
            reservationDateText.text = "Reservation date: ${formatDate(booking.startDate as? Long)}"

            // Static check-in and check-out times (dynamic if needed)
            checkInText.text = "Check-in: 8 AM"
            checkOutText.text = "Check-out: 8 PM"

            // Set room type and total price
            roomTypeText.text = "1x ${booking.roomTitle}" // Assuming 1 room
            totalPriceText.text = "₱${booking.totalPrice}"

            // Set payment status
            paymentStatusText.text = booking.paymentStatus
            paymentStatusText.setTextColor(
                when (booking.paymentStatus) {
                    "Paid" -> itemView.context.getColor(android.R.color.holo_green_dark)
                    "Pending Approval" -> itemView.context.getColor(android.R.color.holo_orange_dark)
                    "Rescheduled" -> itemView.context.getColor(android.R.color.holo_blue_dark)
                    else -> itemView.context.getColor(android.R.color.holo_red_dark) // Default for Canceled
                }
            )

            // Handle Paid button click
            paidButton.setOnClickListener {
                // Mark the booking as paid (update database here)
                // TODO: Implement Firebase logic to update the payment status to "Paid"
            }

            // Handle Cancel button click
            cancelButton.setOnClickListener {
                // Mark the booking as canceled (update database here)
                // TODO: Implement Firebase logic to update the payment status to "Canceled"
            }
        }

        // Helper function to format date (with null safety)
        private fun formatDate(dateMillis: Long?): String {
            return if (dateMillis != null) {
                val date = Date(dateMillis)
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                formatter.format(date)
            } else {
                "Invalid Date"
            }
        }
    }
}
