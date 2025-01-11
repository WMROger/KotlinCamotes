package com.example.kotlinactivities.adminPage.adminAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R

class AmenitiesAdapter(private val amenities: List<String>) :
    RecyclerView.Adapter<AmenitiesAdapter.AmenitiesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmenitiesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) // Default layout
        return AmenitiesViewHolder(view)
    }

    override fun onBindViewHolder(holder: AmenitiesViewHolder, position: Int) {
        holder.bind(amenities[position])
    }

    override fun getItemCount(): Int = amenities.size

    class AmenitiesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(amenity: String) {
            textView.text = amenity
        }
    }
}
