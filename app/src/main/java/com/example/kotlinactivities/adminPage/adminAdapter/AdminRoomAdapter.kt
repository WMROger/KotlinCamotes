package com.example.kotlinactivities.adminPage.adminAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminPage.adminRoom.AddRoomFragment

class AdminRoomAdapter(
    private var rooms: List<AddRoomFragment.Room>,
    private val onEditClick: (AddRoomFragment.Room) -> Unit, // Callback for edit action
    private val onDeleteClick: (AddRoomFragment.Room) -> Unit // Callback for delete action
) : RecyclerView.Adapter<AdminRoomAdapter.AdminRoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_room_card, parent, false) // New layout for admin rooms
        return AdminRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminRoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.bind(room, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int = rooms.size

    fun updateRooms(updatedRooms: List<AddRoomFragment.Room>) {
        rooms = updatedRooms
        notifyDataSetChanged()
    }

    class AdminRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomImage: ImageView = itemView.findViewById(R.id.roomImage)
        private val roomName: TextView = itemView.findViewById(R.id.roomTitle)
        private val roomRating: TextView = itemView.findViewById(R.id.roomRating)
        private val roomPeople: TextView = itemView.findViewById(R.id.roomPeople)
        private val roomPrice: TextView = itemView.findViewById(R.id.roomPrice)
//        private val editButton: Button = itemView.findViewById(R.id.editButton)
//        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(
            room: AddRoomFragment.Room,
            onEditClick: (AddRoomFragment.Room) -> Unit,
            onDeleteClick: (AddRoomFragment.Room) -> Unit
        ) {
            roomImage.setImageResource(room.imageResId)
            roomName.text = room.name
            roomRating.text = "${room.rating} ★"
            roomPeople.text = "People: ${room.maxPerson}"
            roomPrice.text = room.price

//            // Handle Edit Button Click
//            editButton.setOnClickListener {
//                onEditClick(room)
//            }
//
//            // Handle Delete Button Click
//            deleteButton.setOnClickListener {
//                onDeleteClick(room)
//            }
        }
    }
}
