package com.example.kotlinactivities.adminPage.adminAdapter.customRoomAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R

class SingleSelectionCategoryAdapter(
    private val categories: List<String>,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<SingleSelectionCategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION // Track selected category position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_custom_room_category, parent, false) // Use your item layout here
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position

            // Notify adapter to update UI
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            // Trigger the callback with the selected category
            onCategorySelected(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)

        fun bind(category: String, isSelected: Boolean) {
            categoryName.text = category
            categoryName.setTextColor(
                if (isSelected) itemView.context.getColor(R.color.green) // Highlight the selected item
                else itemView.context.getColor(android.R.color.black) // Default color
            )
        }
    }
}
