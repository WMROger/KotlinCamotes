package com.example.kotlinactivities.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.ImageCarouselAdapter
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

        // Handle Room Card Click
        holder.itemView.setOnClickListener {
            onRoomClick(room) // Calls the lambda passed from HomeFragment
        }
    }

    override fun getItemCount(): Int = roomList.size

    fun updateRooms(newRooms: List<Room>) {
        roomList.clear()
        roomList.addAll(newRooms)
        notifyDataSetChanged()
    }

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomCarousel: ViewPager2 = itemView.findViewById(R.id.roomCarousel)
        private val roomTitle: TextView = itemView.findViewById(R.id.roomTitle)
        private val roomPeople: TextView = itemView.findViewById(R.id.roomPeople)
        private val roomPrice: TextView = itemView.findViewById(R.id.roomPrice)
        private val roomRating: TextView = itemView.findViewById(R.id.roomRating)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
        fun bind(room: Room) {
            // Handle multiple images or fallback to single image
            val imageUrls = room.imageUrls?.ifEmpty { listOf(room.imageUrl) } ?: listOf(room.imageUrl)
            roomCarousel.adapter = ImageCarouselAdapter(imageUrls)

            // Set other room details with fallback/default values
            roomTitle.text = room.title.ifBlank { "Untitled Room" }
            roomPeople.text = if (room.people.isNotBlank()) "${room.people} People" else "N/A People"
            roomPrice.text = room.price.ifBlank { "₱0/night" }
            roomRating.text = room.rating.ifBlank { "No Rating" }
            // Update favorite button icon based on the isFavorited state
            favoriteButton.setImageResource(
                if (room.isFavorited) R.drawable.ic_heart else R.drawable.ic_heart_black
            )

            // Handle favorite button toggle
            favoriteButton.setOnClickListener {
                room.isFavorited = !room.isFavorited
                favoriteButton.setImageResource(
                    if (room.isFavorited) R.drawable.ic_heart else R.drawable.ic_heart_black
                )

                // Log the favorite status for debugging
                Log.d("RoomAdapter", "Room ${room.title} favorited: ${room.isFavorited}")
            }
        }
    }
}
