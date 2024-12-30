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
            Log.d("RoomAdapter", "Room clicked: ${room.title}")
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
        private val roomCarousel: ViewPager2 = itemView.findViewById(R.id.roomCarousel) // Carousel for images
        private val roomTitle: TextView = itemView.findViewById(R.id.roomTitle)
        private val roomPeople: TextView = itemView.findViewById(R.id.roomPeople)
        private val roomPrice: TextView = itemView.findViewById(R.id.roomPrice)
        private val roomRating: TextView = itemView.findViewById(R.id.roomRating)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)

        fun bind(room: Room) {
            // Ensure imageUrls is never null
            val images = room.imageUrls ?: emptyList()
            roomCarousel.adapter = ImageCarouselAdapter(images) // Pass non-null list to adapter

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

            // Handle favorite button click
            favoriteButton.setOnClickListener {
                room.isFavorited = !room.isFavorited
                // Change icon dynamically
                if (room.isFavorited) {
                    favoriteButton.setImageResource(R.drawable.ic_heart)
                } else {
                    favoriteButton.setImageResource(R.drawable.ic_heart_black)
                }
            }
        }

    }
}
