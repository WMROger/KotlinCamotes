package com.example.kotlinactivities.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlinactivities.R
import com.example.kotlinactivities.model.Room

class MyRoomsAdapter(
    private val rooms: MutableList<Room>,
    private val onFavoriteClicked: (Room) -> Unit,
    private val onDeleteClicked: (Room) -> Unit,
    private val onItemClicked: (Room, String) -> Unit // Callback for item click with booking status
) : RecyclerView.Adapter<MyRoomsAdapter.MyRoomsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRoomsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_room, parent, false)
        return MyRoomsViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyRoomsViewHolder, position: Int) {
        val room = rooms[position]

        // Load room image with Glide
        Glide.with(holder.itemView.context)
            .load(room.imageUrl)
            .placeholder(R.drawable.ic_cupids_deluxe)
            .error(R.drawable.ic_splash)
            .into(holder.roomImageView)

        // Set room details
        holder.roomTitleTextView.text = room.title
        holder.roomPeopleTextView.text = room.people
        holder.roomPriceTextView.text = room.price
        holder.roomRatingTextView.text = room.rating

        // Set status badge based on `paymentStatus` and `status` fields
        // Set status badge based on `paymentStatus` and `status` fields
        val paymentStatus = room.bookingStatus // This should already contain the correct derived status

        when {
            paymentStatus.equals("Approved", ignoreCase = true) -> {
                holder.statusBadge.text = "Approved"
                holder.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                holder.statusBadge.setBackgroundResource(R.drawable.badge_approved) // Green background
            }
            paymentStatus.equals("Pending Approval", ignoreCase = true) || paymentStatus.equals("Pending", ignoreCase = true) -> {
                holder.statusBadge.text = "Pending Approval"
                holder.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                holder.statusBadge.setBackgroundResource(R.drawable.badge_pending) // Orange background
            }
            paymentStatus.equals("Rejected", ignoreCase = true) -> {
                holder.statusBadge.text = "Rejected"
                holder.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                holder.statusBadge.setBackgroundResource(R.drawable.badge_rejected) // Red background
            }
            paymentStatus.equals("Cancelled", ignoreCase = true) -> {
                holder.statusBadge.text = "Cancelled"
                holder.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black)) // Set black text color
                holder.statusBadge.setBackgroundResource(R.drawable.badge_cancelled) // Black background (optional or default gray)
            }
            else -> {
                holder.statusBadge.text = "Unknown Status"
                holder.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                holder.statusBadge.setBackgroundResource(R.drawable.badge_unknown) // Gray background
            }
        }


        // Handle item click
        holder.itemView.setOnClickListener {
            onItemClicked(room, room.bookingStatus)
        }
    }


    override fun getItemCount(): Int = rooms.size

    fun removeRoom(room: Room) {
        val position = rooms.indexOf(room)
        if (position != -1) {
            rooms.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class MyRoomsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomImageView: ImageView = itemView.findViewById(R.id.roomImageView)
        val roomTitleTextView: TextView = itemView.findViewById(R.id.roomTitleTextView)
        val roomPeopleTextView: TextView = itemView.findViewById(R.id.roomPeopleTextView)
        val roomPriceTextView: TextView = itemView.findViewById(R.id.roomPriceTextView)
        val roomRatingTextView: TextView = itemView.findViewById(R.id.roomRatingTextView)
        val statusBadge: TextView = itemView.findViewById(R.id.statusBadge) // Add status badge view
    }
}
