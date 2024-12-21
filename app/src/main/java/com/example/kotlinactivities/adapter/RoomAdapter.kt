package com.example.kotlinactivities.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.databinding.RoomCardBinding
import com.example.kotlinactivities.model.Room

class RoomAdapter(private val roomList: List<Room>) :
    RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(private val binding: RoomCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(room: Room) {
            binding.roomImage.setImageResource(room.imageUrl)
            binding.roomTitle.text = room.title
            binding.roomPeople.text = "People: ${room.people}"
            binding.roomPrice.text = room.price
            binding.roomRating.text = "Rating: ${room.rating}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = RoomCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(roomList[position])
    }

    override fun getItemCount(): Int = roomList.size
}
