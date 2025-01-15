package com.example.kotlinactivities.adminPage.adminAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.kotlinactivities.R
import com.example.kotlinactivities.model.AdminRoom // Import AdminRoom model

class AdminRoomAdapter(private var rooms: List<AdminRoom>) :
    RecyclerView.Adapter<AdminRoomAdapter.RoomViewHolder>() { // Change RoomAdapter to AdminRoomAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_room_card, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount(): Int = rooms.size

    fun updateRooms(updatedRooms: List<AdminRoom>) {
        rooms = updatedRooms
        notifyDataSetChanged()
    }

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomCarousel: ViewPager2 = itemView.findViewById(R.id.roomCarousel)
        private val roomTitle: TextView = itemView.findViewById(R.id.roomTitle)
        private val roomRating: TextView = itemView.findViewById(R.id.roomRating)
        private val roomPeople: TextView = itemView.findViewById(R.id.roomPeople)
        private val roomPrice: TextView = itemView.findViewById(R.id.roomPrice)

        fun bind(room: AdminRoom) {
            // Set up carousel adapter
            roomCarousel.adapter = RoomCarouselAdapter(listOf(room.imageUrl))

            // Bind room data
            roomTitle.text = room.name
            roomRating.text = "${room.rating} ★"
            roomPeople.text = "People: ${room.maxPerson}"
            roomPrice.text = room.price
        }
    }
}
