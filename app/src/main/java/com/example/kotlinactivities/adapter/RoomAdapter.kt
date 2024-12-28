package com.example.kotlinactivities.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlinactivities.R
import com.example.kotlinactivities.model.Room

class RoomAdapter(
    private var roomList: MutableList<Room>, // Mutable list for data
    private val onDeleteClick: (Room) -> Unit, // Callback for delete button click
    private val onRoomClick: (Room) -> Unit, // Callback for room card click
    private val isMyRoomsContext: Boolean // Indicates if adapter is for MyRoomFragment
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_card, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.bind(room, isMyRoomsContext)

        // Handle Delete Button Click (if in MyRoomFragment)
        holder.deleteButton.setOnClickListener {
            onDeleteClick(room) // Trigger the delete callback
        }

        // Handle Item (Card) Click
        holder.itemView.setOnClickListener {
            onRoomClick(room) // Trigger the room click callback
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
        val deleteButton: ImageView = itemView.findViewById(R.id.favoriteButton) // Reuse favoriteButton as deleteButton

        // Update icon and data dynamically
        fun bind(room: Room, isMyRoomsContext: Boolean) {
            // Load image from URL using Glide
            Glide.with(itemView.context)
                .load(room.imageUrl)
                .placeholder(R.drawable.ic_cupids_deluxe) // Optional placeholder image
                .error(R.drawable.ic_splash) // Optional error image
                .into(roomImage)

            roomTitle.text = room.title
            roomPeople.text = "${room.people} People"
            roomPrice.text = room.price
            roomRating.text = room.rating

            // Set the button icon based on the adapter context
            if (isMyRoomsContext) {
                deleteButton.setImageResource(R.drawable.ic_cash) // Show delete icon for MyRoomFragment
            } else {
                deleteButton.setImageResource(R.drawable.ic_heart) // Show favorite icon otherwise
            }
        }
    }
}
