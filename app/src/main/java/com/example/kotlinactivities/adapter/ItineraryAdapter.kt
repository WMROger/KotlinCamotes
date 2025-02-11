package com.example.kotlinactivities.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.model.ItineraryItem

class ItineraryAdapter(private val items: List<ItineraryItem>) :
    RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder>() {

    class ItineraryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val imageView: ImageView = view.findViewById(R.id.itineraryImage)
        val title: TextView = view.findViewById(R.id.itineraryTitle)
        val description: TextView = view.findViewById(R.id.itineraryDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_itinerary, parent, false)
        return ItineraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        val item = items[position]

        // Set content
        holder.imageView.setImageResource(item.imageResId)
        holder.title.text = item.title
        holder.description.text = item.description
    }

    override fun getItemCount(): Int = items.size
}
