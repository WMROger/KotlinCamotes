package com.example.kotlinactivities.adminadapter.customRoomAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R

class CustomRoomAmenitiesAdapter(
    amenities: List<String>,
    private val onAmenitySelected: (Set<String>) -> Unit
) : RecyclerView.Adapter<CustomRoomAmenitiesAdapter.AmenitiesViewHolder>() {

    private val sortedAmenities = amenities.sorted() // Sort amenities alphabetically
    private val selectedAmenities = mutableSetOf<String>() // Track selected amenities

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmenitiesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room_amenities, parent, false)
        return AmenitiesViewHolder(view)
    }

    override fun onBindViewHolder(holder: AmenitiesViewHolder, position: Int) {
        val amenity = sortedAmenities[position]
        holder.bind(amenity, selectedAmenities.contains(amenity))

        holder.itemView.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            if (selectedAmenities.contains(amenity)) {
                selectedAmenities.remove(amenity)
            } else {
                selectedAmenities.add(amenity)
            }

            notifyItemChanged(currentPosition)
            onAmenitySelected(selectedAmenities)
        }
    }

    override fun getItemCount(): Int = sortedAmenities.size

    class AmenitiesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val amenityName: TextView = itemView.findViewById(R.id.amenityName)

        fun bind(amenity: String, isSelected: Boolean) {
            amenityName.text = amenity
            amenityName.textSize = 12.45f // Set text size to 12.45sp
            amenityName.setTextColor(
                if (isSelected) itemView.context.getColor(R.color.green)
                else itemView.context.getColor(R.color.black)
            )
        }
    }
}
