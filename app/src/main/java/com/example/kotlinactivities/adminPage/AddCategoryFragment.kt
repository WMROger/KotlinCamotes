package com.example.kotlinactivities.adminPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R

class AddCategoryFragment : Fragment() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var addCategoryButton: Button
    private lateinit var categoryInput: EditText
    private lateinit var categoryAdapter: CategoryAdapter
    private val categoryList = mutableListOf("Barkada", "Deluxe", "VIP") // Initial categories

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_category, container, false)

        // Initialize views
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
        addCategoryButton = view.findViewById(R.id.addCategoryButton)
        categoryInput = view.findViewById(R.id.categoryInput)

        // Set up RecyclerView
        categoryAdapter = CategoryAdapter(categoryList)
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)
        categoryRecyclerView.adapter = categoryAdapter

        // Handle adding a new category
        addCategoryButton.setOnClickListener {
            val newCategory = categoryInput.text.toString().trim()
            if (newCategory.isNotEmpty()) {
                categoryList.add(newCategory)
                categoryAdapter.notifyItemInserted(categoryList.size - 1)
                categoryInput.text.clear()
                Toast.makeText(context, "Category added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Enter a category name", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Adapter for the RecyclerView
    class CategoryAdapter(private val categories: List<String>) :
        RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category, parent, false)
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            holder.bind(categories[position])
        }

        override fun getItemCount(): Int = categories.size

        class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val categoryName: TextView = itemView.findViewById(R.id.categoryName)

            fun bind(category: String) {
                categoryName.text = category
            }
        }
    }
}
