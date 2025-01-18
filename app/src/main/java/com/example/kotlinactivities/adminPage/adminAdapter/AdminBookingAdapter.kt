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
    private val isUpcomingTab: Boolean // Pass whether it's the "Upcoming" tab
) : RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder>() {

    private var expandedPosition: Int = -1 // To track the expanded card in the "Upcoming" tab

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
            .inflate(R.layout.item_booking, parent, false) // Match the item_booking layout
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        // Set data in the view
        holder.roomType.text = booking.roomTitle ?: "Unknown"
        holder.reservationDate.text = "Reservation date: ${booking.startDateReadable} - ${booking.endDateReadable}"
        holder.checkIn.text = "Check-in: 8am" // Hardcoded check-in time
        holder.checkOut.text = "Check-out: 8pm" // Hardcoded check-out time
        holder.totalPrice.text = "₱${booking.totalPrice ?: 0}"

        // Map paymentStatus to a more user-friendly display value
        holder.paymentStatus.text = when (booking.paymentStatus?.lowercase(Locale.ROOT)) {
            "success" -> "Paid - via GCash"
            "accepted" -> "Accepted"
            "pending approval" -> "Pending Approval"
            "rescheduled" -> "Rescheduled"
            else -> booking.paymentStatus ?: "Unknown"
        }

        // Fetch and display user's full name if needed
        booking.userId?.let { userId ->
            fetchUserName(userId) { name ->
                holder.userName.text = name
            }
        }

        // Show/hide buttons logic (if applicable for the tab)
        if (!isUpcomingTab) {
            holder.paidButton.visibility = View.GONE
            holder.cancelButton.visibility = View.GONE
        } else {
            if (position == expandedPosition) {
                holder.paidButton.visibility = View.VISIBLE
                holder.cancelButton.visibility = View.VISIBLE
            } else {
                holder.paidButton.visibility = View.GONE
                holder.cancelButton.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                val previousExpandedPosition = expandedPosition
                expandedPosition = if (expandedPosition == position) -1 else position
                notifyItemChanged(previousExpandedPosition)
                notifyItemChanged(position)
            }
        }
    }


    override fun getItemCount(): Int = bookings.size
}
