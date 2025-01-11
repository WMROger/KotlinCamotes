package com.example.kotlinactivities.adminPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminPage.ViewModel.CategoryViewModel
import com.example.kotlinactivities.adminPage.adminAdapter.CategoryAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddCategoryFragment : Fragment() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var addCategoryButton: Button
    private lateinit var categoryInput: EditText
    private lateinit var categoryAdapter: CategoryAdapter
    private val categoryViewModel: CategoryViewModel by activityViewModels()

    // Firebase database reference
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_category, container, false)

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("categories")

        // Initialize views
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
        addCategoryButton = view.findViewById(R.id.addCategoryButton)
        categoryInput = view.findViewById(R.id.categoryInput)

        // Initialize the adapter
        categoryAdapter = CategoryAdapter(mutableListOf()) { categoryViewModel.removeCategory(it) }
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)
        categoryRecyclerView.adapter = categoryAdapter

        // Observe ViewModel categories
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.updateCategories(categories)
        }

        // Add new category
        addCategoryButton.setOnClickListener {
            val newCategory = categoryInput.text.toString().trim()
            if (newCategory.isNotEmpty()) {
                // Add to ViewModel
                categoryViewModel.addCategory(newCategory)

                // Save to Firebase
                saveCategoryToFirebase(newCategory)

                // Clear input field and show a message
                categoryInput.text.clear()
                Toast.makeText(context, "Category added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Enter a category name", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun saveCategoryToFirebase(category: String) {
        // Generate a unique key for the category
        val categoryId = databaseReference.push().key

        if (categoryId != null) {
            databaseReference.child(categoryId).setValue(category)
                .addOnSuccessListener {
                    Toast.makeText(context, "Category saved to Firebase", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to save category: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Failed to generate category ID", Toast.LENGTH_SHORT).show()
        }
    }
}
