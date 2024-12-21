package com.example.kotlinactivities.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlinactivities.model.Room
import com.example.kotlinactivities.R

class RoomAdapter(private val roomList: List<Room>) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomImage: ImageView = itemView.findViewById(R.id.roomImage)
        val roomTitle: TextView = itemView.findViewById(R.id.roomTitle)
        val roomDetails: TextView = itemView.findViewById(R.id.roomDetails)
        val roomRating: TextView = itemView.findViewById(R.id.roomRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_card, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]

        // Bind room data
        holder.roomTitle.text = room.title
        holder.roomDetails.text = "${room.people} person · ${room.price}"
        holder.roomRating.text = "${room.rating} ★"

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(room.imageUrl)
            .placeholder(R.drawable.ic_map) // Replace with a valid placeholder drawable
            .error(R.drawable.ic_home)       // Replace with a valid error drawable
            .into(holder.roomImage)
    }


    override fun getItemCount(): Int = roomList.size
}
