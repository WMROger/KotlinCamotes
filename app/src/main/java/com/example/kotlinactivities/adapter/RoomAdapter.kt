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
    private var roomList: MutableList<Room>,
    private val onDeleteClick: (Room) -> Unit,
    private val onRoomClick: (Room) -> Unit,
    private val isMyRoomsContext: Boolean
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_card, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.bind(room)

        // Handle Delete Button Click (if in MyRoomFragment)
        holder.favoriteButton.setOnClickListener {
            // Toggle the favorite state
            room.isFavorited = !room.isFavorited
            notifyItemChanged(position) // Refresh the specific item in the RecyclerView
        }

        // Handle Room Card Click
        holder.itemView.setOnClickListener {
            onRoomClick(room)
        }
    }

    override fun getItemCount(): Int = roomList.size

    fun updateRooms(newRooms: List<Room>) {
        roomList.clear()
        roomList.addAll(newRooms)
        notifyDataSetChanged()
    }

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomImage: ImageView = itemView.findViewById(R.id.roomImage)
        val roomTitle: TextView = itemView.findViewById(R.id.roomTitle)
        val roomPeople: TextView = itemView.findViewById(R.id.roomPeople)
        val roomPrice: TextView = itemView.findViewById(R.id.roomPrice)
        val roomRating: TextView = itemView.findViewById(R.id.roomRating)
        val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton) // Correct ID

        fun bind(room: Room) {
            // Load image with Glide
            Glide.with(itemView.context)
                .load(room.imageUrl)
                .placeholder(R.drawable.ic_cupids_deluxe) // Placeholder image
                .error(R.drawable.ic_splash) // Error fallback image
                .into(roomImage)

            roomTitle.text = room.title
            roomPeople.text = "${room.people} People"
            roomPrice.text = room.price
            roomRating.text = room.rating

            // Set the favorite button icon based on the favorite state
            if (room.isFavorited) {
                favoriteButton.setImageResource(R.drawable.ic_heart) // Favorited icon (Red Heart)
            } else {
                favoriteButton.setImageResource(R.drawable.ic_heart_black) // Unfavorited icon (Black Heart)
            }
        }
    }
}
