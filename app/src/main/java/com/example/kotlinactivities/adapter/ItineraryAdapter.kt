package com.example.kotlinactivities.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.model.ItineraryItem

class ItineraryAdapter(private val itineraryList: List<ItineraryItem>) :
    RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder>() {

    class ItineraryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.itineraryImage)
        val title: TextView = view.findViewById(R.id.itineraryTitle)
        val description: TextView = view.findViewById(R.id.itineraryDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_itinerary, parent, false)
        return ItineraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        val item = itineraryList[position]
        holder.image.setImageResource(item.imageResId)
        holder.title.text = item.title
        holder.description.text = item.description
    }

    override fun getItemCount() = itineraryList.size
}
