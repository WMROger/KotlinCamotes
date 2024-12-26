package com.example.kotlinactivities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R

class RoomImageAdapter(
    private val context: Context,
    private val images: List<Int>
) : RecyclerView.Adapter<RoomImageAdapter.RoomImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_room_image, parent, false)
        return RoomImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomImageViewHolder, position: Int) {
        holder.imageView.setImageResource(images[position])
    }

    override fun getItemCount(): Int = images.size

    class RoomImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.roomImageView)
    }
}
