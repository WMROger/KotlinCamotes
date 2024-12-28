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

class MyRoomsAdapter(
    private val rooms: List<Room>,
    private val onFavoriteClicked: (Room) -> Unit
) : RecyclerView.Adapter<MyRoomsAdapter.MyRoomsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRoomsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_room, parent, false)
        return MyRoomsViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyRoomsViewHolder, position: Int) {
        val room = rooms[position]

        // Use Glide to load the image from the URL
        Glide.with(holder.itemView.context)
            .load(room.imageUrl) // Image URL
            .placeholder(R.drawable.ic_cupids_deluxe) // Optional placeholder image
            .error(R.drawable.ic_splash) // Optional error image
            .into(holder.roomImageView)

        // Bind other data to views
        holder.roomTitleTextView.text = room.title
        holder.roomPeopleTextView.text = room.people
        holder.roomPriceTextView.text = room.price
        holder.roomRatingTextView.text = room.rating

        // Handle favorite button click
        holder.favoriteButton.setOnClickListener {
            onFavoriteClicked(room)
        }
    }

    override fun getItemCount(): Int = rooms.size

    class MyRoomsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomImageView: ImageView = itemView.findViewById(R.id.roomImageView)
        val roomTitleTextView: TextView = itemView.findViewById(R.id.roomTitleTextView)
        val roomPeopleTextView: TextView = itemView.findViewById(R.id.roomPeopleTextView)
        val roomPriceTextView: TextView = itemView.findViewById(R.id.roomPriceTextView)
        val roomRatingTextView: TextView = itemView.findViewById(R.id.roomRatingTextView)
        val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
    }
}
