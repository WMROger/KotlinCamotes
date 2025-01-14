package com.example.kotlinactivities.adminPage.adminRoom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminPage.adminAdapter.CategoryAdapter
import com.google.firebase.database.*

class AddCategoryFragment : Fragment() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var addCategoryButton: Button
    private lateinit var categoryInput: EditText
    private lateinit var categoryAdapter: CategoryAdapter

    // Firebase database reference
    private lateinit var databaseReference: DatabaseReference

    private val categories = mutableListOf<String>() // Store categories locally

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
        categoryAdapter = CategoryAdapter(categories) { category ->
            removeCategoryFromFirebase(category)
        }
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)
        categoryRecyclerView.adapter = categoryAdapter

        // Fetch data from Firebase
        fetchCategoriesFromFirebase()

        // Add new category
        addCategoryButton.setOnClickListener {
            val newCategory = categoryInput.text.toString().trim()
            if (newCategory.isNotEmpty()) {
                saveCategoryToFirebase(newCategory)
                categoryInput.text.clear()
                Toast.makeText(context, "Category added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Enter a category name", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun saveCategoryToFirebase(category: String) {
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

    private fun fetchCategoriesFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categories.clear()
                for (child in snapshot.children) {
                    val category = child.getValue(String::class.java)
                    if (category != null) {
                        categories.add(category)
                    }
                }
                categoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removeCategoryFromFirebase(category: String) {
        val cleanCategory = category.trim()

        databaseReference.orderByValue().equalTo(cleanCategory)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("RemoveCategory", "Snapshot exists: ${snapshot.exists()}")
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            Log.d("RemoveCategory", "Removing key: ${child.key}, value: ${child.value}")
                            child.ref.removeValue()
                                .addOnSuccessListener {
                                    Log.d("RemoveCategory", "Successfully removed: $cleanCategory")
                                    Toast.makeText(context, "Category removed successfully", Toast.LENGTH_SHORT).show()
                                    categories.remove(cleanCategory)
                                    categoryAdapter.notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("RemoveCategory", "Failed to remove: ${e.message}")
                                    Toast.makeText(context, "Failed to remove category: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Log.d("RemoveCategory", "Category not found: $cleanCategory")
                        Toast.makeText(context, "Category not found in the database", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RemoveCategory", "Database error: ${error.message}")
                    Toast.makeText(context, "Failed to remove category: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


}
