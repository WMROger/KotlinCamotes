package com.example.kotlinactivities.adapter

import android.util.Log
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

class BookingAdapter(
    private val bookings: List<Booking>,
    private val fetchUserName: (String, (String) -> Unit) -> Unit // Pass a function to fetch the user's name
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

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
            Log.d("BookingAdapter", "Binding userId: ${booking.userId}") // Debug userId

            // Fetch and display the user's first name
            fetchUserName(booking.userId) { fullName ->
                val firstName = fullName.split(" ").firstOrNull() ?: "Unknown"
                userNameText.text = firstName
            }

            // Format reservation date
            reservationDateText.text = "Reservation date: ${formatDate(booking.startDate)}"

            // Static check-in and check-out times
            checkInText.text = "Check-in: 8 AM"
            checkOutText.text = "Check-out: 8 PM"

            // Set room type and total price
            roomTypeText.text = "1x ${booking.roomTitle}"
            totalPriceText.text = "₱${booking.totalPrice}"

            // Set payment status
            paymentStatusText.text = booking.paymentStatus
            paymentStatusText.setTextColor(
                when (booking.paymentStatus) {
                    "Paid" -> itemView.context.getColor(android.R.color.holo_green_dark)
                    "Pending Approval" -> itemView.context.getColor(android.R.color.holo_orange_dark)
                    "Rescheduled" -> itemView.context.getColor(android.R.color.holo_blue_dark)
                    "Cancelled" -> itemView.context.getColor(android.R.color.black) // Set black color for "Cancelled"
                    else -> itemView.context.getColor(android.R.color.holo_red_dark)
                }
            )

            // Set text to "Cancelled" when the status is "Cancelled"
            if (booking.paymentStatus.equals("Cancelled", ignoreCase = true)) {
                paymentStatusText.text = "Cancelled"
            }


            // Handle button clicks
            paidButton.setOnClickListener {
                // TODO: Implement Firebase logic to mark as Paid
            }
            cancelButton.setOnClickListener {
                // TODO: Implement Firebase logic to Cancel the booking
            }
        }


        // Helper function to format date
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
