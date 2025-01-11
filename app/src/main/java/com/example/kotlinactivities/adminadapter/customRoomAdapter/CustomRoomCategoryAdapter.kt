package com.example.kotlinactivities.adminadapter.customRoomAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R

class CustomRoomCategoryAdapter(
    categories: List<String>,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CustomRoomCategoryAdapter.CategoryViewHolder>() {

    private val sortedCategories = categories.sorted() // Sort categories alphabetically
    private var selectedPosition = RecyclerView.NO_POSITION // Track the selected category position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = sortedCategories[position]
        holder.bind(category, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val previousPosition = selectedPosition
            selectedPosition = currentPosition

            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            onCategorySelected(category)
        }
    }

    override fun getItemCount(): Int = sortedCategories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)

        fun bind(category: String, isSelected: Boolean) {
            categoryName.text = category
            categoryName.textSize = 12.45f // Set text size to 12.45sp
            categoryName.setTextColor(
                if (isSelected) itemView.context.getColor(R.color.green)
                else itemView.context.getColor(R.color.black)
            )
        }
    }
}
