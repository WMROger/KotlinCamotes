package com.example.kotlinactivities.adminPage.adminRoom

import android.os.Bundle
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
import com.example.kotlinactivities.adminPage.adminAdapter.AmenitiesAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddAmenitiesFragment : Fragment() {

    private lateinit var rvAmenitiesList: RecyclerView
    private lateinit var etAddAmenity: EditText
    private lateinit var btnSaveAmenity: Button
    private lateinit var amenitiesAdapter: AmenitiesAdapter
    private lateinit var databaseReference: DatabaseReference // Firebase Reference

    private val amenities = mutableListOf<String>() // Local list of amenities

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_amenities, container, false)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("amenities")

        // Initialize views
        rvAmenitiesList = view.findViewById(R.id.rvAmenitiesList)
        etAddAmenity = view.findViewById(R.id.etAddAmenity)
        btnSaveAmenity = view.findViewById(R.id.btnSaveAmenity)

        // Set up RecyclerView
        amenitiesAdapter = AmenitiesAdapter(amenities)
        rvAmenitiesList.layoutManager = LinearLayoutManager(context)
        rvAmenitiesList.adapter = amenitiesAdapter

        // Load amenities from Firebase
        fetchAmenitiesFromFirebase()

        // Handle Save button click
        btnSaveAmenity.setOnClickListener {
            val newAmenity = etAddAmenity.text.toString().trim()
            if (newAmenity.isNotEmpty()) {
                addAmenityToFirebase(newAmenity)
                etAddAmenity.text.clear() // Clear input
            } else {
                Toast.makeText(context, "Please enter an amenity.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun fetchAmenitiesFromFirebase() {
        databaseReference.get().addOnSuccessListener { snapshot ->
            amenities.clear()
            for (child in snapshot.children) {
                val amenity = child.getValue(String::class.java)
                if (amenity != null) {
                    amenities.add(amenity)
                }
            }
            amenitiesAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load amenities.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addAmenityToFirebase(amenity: String) {
        val key = databaseReference.push().key // Generate a unique key
        if (key != null) {
            databaseReference.child(key).setValue(amenity)
                .addOnSuccessListener {
                    amenities.add(amenity) // Update local list
                    amenitiesAdapter.notifyItemInserted(amenities.size - 1)
                    Toast.makeText(context, "Amenity added successfully.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to add amenity.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
