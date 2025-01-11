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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminPage.ViewModel.CategoryViewModel

class AddCategoryFragment : Fragment() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var addCategoryButton: Button
    private lateinit var categoryInput: EditText
    private lateinit var categoryAdapter: CategoryAdapter
    private val categoryViewModel: CategoryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_category, container, false)

        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
        addCategoryButton = view.findViewById(R.id.addCategoryButton)
        categoryInput = view.findViewById(R.id.categoryInput)

        categoryAdapter = CategoryAdapter(mutableListOf()) { categoryViewModel.removeCategory(it) }
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)
        categoryRecyclerView.adapter = categoryAdapter

        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.updateCategories(categories)
        }

        addCategoryButton.setOnClickListener {
            val newCategory = categoryInput.text.toString().trim()
            if (newCategory.isNotEmpty()) {
                categoryViewModel.addCategory(newCategory)
                categoryInput.text.clear()
                Toast.makeText(context, "Category added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Enter a category name", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    class CategoryAdapter(private val categories: MutableList<String>, private val onRemove: (String) -> Unit) :
        RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            holder.bind(categories[position])
        }

        override fun getItemCount(): Int = categories.size

        fun updateCategories(newCategories: List<String>) {
            categories.clear()
            categories.addAll(newCategories)
            notifyDataSetChanged()
        }

        class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val categoryName: TextView = itemView.findViewById(R.id.categoryName)
            private val removeButton: Button = itemView.findViewById(R.id.removeCategoryButton)

            fun bind(category: String) {
                categoryName.text = category
                removeButton.setOnClickListener {
                    onRemove(category)
                }
            }
        }
    }
}
