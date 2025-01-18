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
            .inflate(R.layout.item_todays_booking, parent, false) // Match the new XML layout
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        // Set booking details
        holder.userName.text = booking.userName ?: "Unknown"
        holder.reservationDate.text = "Reservation date: ${booking.startDateReadable ?: "N/A"} - ${booking.endDateReadable ?: "N/A"}"
        holder.checkIn.text = "Check-in: 8am"
        holder.checkOut.text = "Check-out: 8pm"
        holder.roomType.text = booking.roomTitle ?: "1x Deluxe Room"
        holder.paymentStatus.text = booking.paymentStatus ?: "Unknown"
        holder.totalPrice.text = "₱${booking.totalPrice ?: "0"}"

        // Optional: Set user profile image (if applicable)
        // You can load the image using a library like Glide or Picasso
        // Example:
        // Glide.with(holder.userProfileImage.context)
        //     .load(booking.imageUrl ?: R.drawable.ic_profile)
        //     .circleCrop()
        //     .into(holder.userProfileImage)

        // Fetch and display the user's name if needed
        booking.userId?.let { userId ->
            fetchUserName(userId) { name ->
                holder.userName.text = name
            }
        }
    }

    override fun getItemCount(): Int = bookings.size
}
