package com.example.kotlinactivities.adminPage.adminAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.model.AdminBooking

class AdminBookingAdapter(
    private val bookings: List<AdminBooking>,
    private val fetchUserName: (String, (String) -> Unit) -> Unit
) : RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder>() {

    // ViewHolder definition
    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userProfileImage: ImageView = view.findViewById(R.id.userProfileImage)
        val userName: TextView = view.findViewById(R.id.userName)
        val reservationDate: TextView = view.findViewById(R.id.reservationDate)
        val checkIn: TextView = view.findViewById(R.id.checkIn)
        val checkOut: TextView = view.findViewById(R.id.checkOut)
        val roomType: TextView = view.findViewById(R.id.roomType)
        val paymentStatus: TextView = view.findViewById(R.id.paymentStatus)
        val totalPrice: TextView = view.findViewById(R.id.totalPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false) // Inflate the correct layout
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        // Bind booking details to the views
        holder.userName.text = "User: ${booking.userId ?: "Unknown"}"
        holder.reservationDate.text = "Reservation Date: ${booking.startDateReadable ?: "N/A"} - ${booking.endDateReadable ?: "N/A"}"
        holder.checkIn.text = "Check-in: 8am" // Replace with dynamic data if available
        holder.checkOut.text = "Check-out: 8pm" // Replace with dynamic data if available
        holder.roomType.text = booking.roomTitle ?: "Unknown Room"
        holder.paymentStatus.text = booking.paymentStatus ?: "Unknown"
        holder.totalPrice.text = "₱${booking.totalPrice ?: 0}"

        // Fetch and display the user's name (if available)
        booking.userId?.let { userId ->
            fetchUserName(userId) { name ->
                holder.userName.text = name
            }
        }

        // Optional: Load profile image if needed (hardcoded here as an example)
        holder.userProfileImage.setImageResource(R.drawable.ic_profile)
    }

    override fun getItemCount(): Int = bookings.size
}
