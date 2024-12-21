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
    private val roomList: List<Room>
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.room_card, parent, false) // Use your layout name
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.titleTextView.text = room.title
        holder.detailsTextView.text = "${room.people} person · ${room.price}"
        holder.ratingTextView.text = "${room.rating} ★"

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(room.imageUrl) // Directly use the resource ID
            .placeholder(R.drawable.ic_map) // Optional placeholder image
            .error(R.drawable.ic_home) // Optional error image
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = roomList.size

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.roomImage)
        val titleTextView: TextView = itemView.findViewById(R.id.roomTitle)
        val detailsTextView: TextView = itemView.findViewById(R.id.roomDetails)
        val ratingTextView: TextView = itemView.findViewById(R.id.roomRating)
    }
}
