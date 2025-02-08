package com.example.kotlinactivities.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
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
        return RoomViewHolder(view, onRoomClick)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.bind(room)
    }

    override fun getItemCount(): Int = roomList.size

    fun updateRooms(newRooms: List<Room>) {
        val diffCallback = RoomDiffCallback(roomList, newRooms)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        roomList.clear()
        roomList.addAll(newRooms)
        diffResult.dispatchUpdatesTo(this)
    }

    class RoomViewHolder(itemView: View, private val onRoomClick: (Room) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val roomCarousel: ViewPager2 = itemView.findViewById(R.id.roomCarousel)
        private val roomTitle: TextView = itemView.findViewById(R.id.roomTitle)
        private val roomPeople: TextView = itemView.findViewById(R.id.roomPeople)
        private val roomPrice: TextView = itemView.findViewById(R.id.roomPrice)
        private val roomRating: TextView = itemView.findViewById(R.id.roomRating)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)

        fun bind(room: Room) {
            // Set default index to avoid recycling issues
            roomCarousel.setCurrentItem(0, false)

            // Ensure images are loaded correctly and safely handle null
            val imageUrls = room.imageUrls?.takeIf { it.isNotEmpty() } ?: listOf(room.imageUrl)
            roomCarousel.adapter = ImageCarouselAdapter(imageUrls)

            // Improve performance with offscreen limit
            roomCarousel.offscreenPageLimit = 1

            // Set other room details
            roomTitle.text = room.title.ifBlank { "Untitled Room" }
            roomPeople.text = if (room.people.isNotBlank()) "${room.people} People" else "N/A People"
            roomPrice.text = room.price.ifBlank { "₱0/night" }
            roomRating.text = room.rating.ifBlank { "No Rating" }

            // Set favorite status
            updateFavoriteIcon(room.isFavorited)

            // Handle favorite button click
            favoriteButton.setOnClickListener {
                room.isFavorited = !room.isFavorited
                updateFavoriteIcon(room.isFavorited)

                // Add fade animation for a smoother transition
                favoriteButton.animate().alpha(0.5f).setDuration(100).withEndAction {
                    favoriteButton.animate().alpha(1f).setDuration(100)
                }

                // Log favorite status
                Log.d("RoomAdapter", "Room ${room.title} favorited: ${room.isFavorited}")
            }

            // Handle room click event
            itemView.setOnClickListener { onRoomClick(room) }
        }


        private fun updateFavoriteIcon(isFavorited: Boolean) {
            favoriteButton.setImageResource(
                if (isFavorited) R.drawable.ic_heart else R.drawable.ic_heart_black
            )
        }
    }

    class RoomDiffCallback(
        private val oldList: List<Room>,
        private val newList: List<Room>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
