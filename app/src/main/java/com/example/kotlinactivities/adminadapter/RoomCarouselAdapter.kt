package com.example.kotlinactivities.adminadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R

class RoomCarouselAdapter(private val imageList: List<Int>) : RecyclerView.Adapter<RoomCarouselAdapter.CarouselViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carousel_image, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int = imageList.size

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.carouselImage)

        fun bind(imageResId: Int) {
            imageView.setImageResource(imageResId)
        }
    }
}
