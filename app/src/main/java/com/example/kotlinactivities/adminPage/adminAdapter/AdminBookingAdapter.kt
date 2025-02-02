package com.example.kotlinactivities.adminPage.adminAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.model.AdminBooking
import java.util.Locale

class AdminBookingAdapter(
    private val bookings: List<AdminBooking>,
    private val fetchUserName: (String, (String) -> Unit) -> Unit,
    private val isUpcomingTab: Boolean,
    private val onPaidClick: (String) -> Unit // Callback for marking as paid
) : RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder>() {

    private var expandedPosition: Int = -1 // Track expanded booking in Upcoming tab

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userProfileImage: ImageView = view.findViewById(R.id.userProfileImage)
        val userName: TextView = view.findViewById(R.id.userName)
        val reservationDate: TextView = view.findViewById(R.id.reservationDate)
        val checkIn: TextView = view.findViewById(R.id.checkIn)
        val checkOut: TextView = view.findViewById(R.id.checkOut)
        val roomType: TextView = view.findViewById(R.id.roomType)
        val paymentStatus: TextView = view.findViewById(R.id.paymentStatus)
        val totalPrice: TextView = view.findViewById(R.id.totalPrice)

        val paidButton: Button = view.findViewById(R.id.paidButton)
        val cancelButton: Button = view.findViewById(R.id.cancelButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        // Set text data
        holder.roomType.text = booking.roomTitle ?: "Unknown"
        holder.reservationDate.text = "Reservation: ${booking.startDateReadable} - ${booking.endDateReadable}"
        holder.checkIn.text = "Check-in: 8am"
        holder.checkOut.text = "Check-out: 8pm"
        holder.totalPrice.text = "₱${booking.totalPrice ?: 0}"

        // Fetch and display user name
        booking.userId?.let { userId ->
            fetchUserName(userId) { name -> holder.userName.text = name }
        }

        // Set payment status text
        holder.paymentStatus.text = when (booking.paymentStatus?.lowercase(Locale.ROOT)) {
            "success" -> "Paid - via GCash"
            "accepted" -> "Accepted"
            "pending approval" -> "Pending Approval"
            "rescheduled" -> "Rescheduled"
            else -> booking.paymentStatus ?: "Unknown"
        }

        // Click handling for "Upcoming" bookings
        if (isUpcomingTab) {
            holder.paidButton.visibility = if (position == expandedPosition) View.VISIBLE else View.GONE
            holder.cancelButton.visibility = if (position == expandedPosition) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener {
                val previousExpandedPosition = expandedPosition
                expandedPosition = if (expandedPosition == position) -1 else position
                notifyItemChanged(previousExpandedPosition)
                notifyItemChanged(position)
            }

            holder.paidButton.setOnClickListener {
                booking.id?.let { id -> onPaidClick(id) }  // Update payment status
            }
        } else {
            // Hide buttons for non-upcoming tabs
            holder.paidButton.visibility = View.GONE
            holder.cancelButton.visibility = View.GONE
        }

        // **Disable clicks for "Today's Bookings"**
        if (!isUpcomingTab && booking.paymentStatus.equals("Success", ignoreCase = true)) {
            holder.itemView.isClickable = false
            holder.itemView.isEnabled = false
            holder.itemView.alpha = 0.7f // Reduce opacity to show it's non-clickable
        } else {
            holder.itemView.isClickable = true
            holder.itemView.isEnabled = true
            holder.itemView.alpha = 1.0f
        }
    }

    override fun getItemCount(): Int = bookings.size
}
