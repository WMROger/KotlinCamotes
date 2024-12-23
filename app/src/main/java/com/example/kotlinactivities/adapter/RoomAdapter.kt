package com.example.kotlinactivities.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.model.Room

class RoomAdapter(
    private var roomList: MutableList<Room>, // Changed to var for mutability
    private val onRoomClick: (Room) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_card, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.bind(room)

        // Handle Favorite Button Click
        holder.favoriteButton.setOnClickListener {
            // Toggle the favorite state
            room.isFavorited = !room.isFavorited

            // Update the icon based on the favorite state
            if (room.isFavorited) {
                holder.favoriteButton.setImageResource(R.drawable.ic_heart) // Favorited state
            } else {
                holder.favoriteButton.setImageResource(R.drawable.ic_heart_black) // Unfavorited state
            }
        }

        // Handle Room Item Click
        holder.itemView.setOnClickListener {
            onRoomClick(room) // Pass the room back to the click callback
        }
    }

    override fun getItemCount(): Int = roomList.size

    // Function to update the list dynamically
    fun updateRooms(newRooms: List<Room>) {
        roomList.clear() // Clear the current list
        roomList.addAll(newRooms) // Add all new items to the list
        notifyDataSetChanged() // Notify the adapter to refresh the RecyclerView
    }

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomImage: ImageView = itemView.findViewById(R.id.roomImage)
        val roomTitle: TextView = itemView.findViewById(R.id.roomTitle)
        val roomPeople: TextView = itemView.findViewById(R.id.roomPeople)
        val roomPrice: TextView = itemView.findViewById(R.id.roomPrice)
        val roomRating: TextView = itemView.findViewById(R.id.roomRating)
        val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)

        fun bind(room: Room) {
            roomImage.setImageResource(room.imageUrl)
            roomTitle.text = room.title
            roomPeople.text = "People: ${room.people}"
            roomPrice.text = room.price
            roomRating.text = room.rating

            // Update the favorite button state
            if (room.isFavorited) {
                favoriteButton.setImageResource(R.drawable.ic_heart) // Favorited
            } else {
                favoriteButton.setImageResource(R.drawable.ic_heart_black) // Unfavorited
            }
        }
    }
}
