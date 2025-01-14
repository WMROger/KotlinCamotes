package com.example.kotlinactivities.adminPage.adminAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R

class CategoryAdapter(
    private var categories: MutableList<String>,
    private val onRemoveCategory: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room_category, parent, false) // Ensure this matches the layout file name
        return CategoryViewHolder(view)
    }


    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryName.text = category

        // Attach the remove click listener to the button
        holder.removeButton.setOnClickListener {
            onRemoveCategory(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<String>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        val removeButton: Button = itemView.findViewById(R.id.removeButton)
    }
}
