package com.example.kotlinactivities.adminPage.adminRoom

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminPage.adminAdapter.AmenitiesAdapter
import com.example.kotlinactivities.adminPage.adminAdapter.CategoryAdapter
import com.example.kotlinactivities.adminPage.adminAdapter.customRoomAdapter.CustomRoomAmenitiesAdapter
import com.example.kotlinactivities.adminPage.adminAdapter.customRoomAdapter.CustomRoomCategoryAdapter
import com.google.firebase.database.*

class AddCustomRoomFragment : Fragment() {

    private lateinit var btnUploadImage: Button
    private lateinit var imagePreviewContainer: LinearLayout
    private lateinit var rvCategory: RecyclerView
    private lateinit var rvAmenities: RecyclerView
    private lateinit var btnDecreasePax: Button
    private lateinit var btnIncreasePax: Button
    private lateinit var tvPaxCount: TextView
    private lateinit var etRoomDescription: EditText
    private lateinit var etRoomPrice: EditText
    private lateinit var btnSaveRoom: Button

    private var paxCount = 5
    private val selectedImages = mutableListOf<Uri>() // Store uploaded image URIs
    private val categories = mutableListOf<String>() // List to store categories
    private val amenities = mutableListOf<String>() // List to store amenities
    private var selectedCategory: String? = null // To store the selected category
    private val selectedAmenities = mutableSetOf<String>() // Track selected amenities

    private lateinit var databaseReference: DatabaseReference // Firebase Database reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_custom_room, container, false)

        // Initialize views
        btnUploadImage = view.findViewById(R.id.btnUploadImage)
        imagePreviewContainer = view.findViewById(R.id.imagePreviewContainer)
        rvCategory = view.findViewById(R.id.rvCategory)
        rvAmenities = view.findViewById(R.id.rvAmenities)
        btnDecreasePax = view.findViewById(R.id.btnDecreasePax)
        btnIncreasePax = view.findViewById(R.id.btnIncreasePax)
        tvPaxCount = view.findViewById(R.id.tvPaxCount)
        etRoomDescription = view.findViewById(R.id.etRoomDescription)
        etRoomPrice = view.findViewById(R.id.etRoomPrice)
        btnSaveRoom = view.findViewById(R.id.btnSaveRoom)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        // Fetch categories and amenities
        fetchCategoriesFromFirebase()
        fetchAmenitiesFromFirebase()

        // Handle Pax Counter
        btnDecreasePax.setOnClickListener {
            if (paxCount > 1) {
                paxCount--
                tvPaxCount.text = paxCount.toString()
            }
        }
        btnIncreasePax.setOnClickListener {
            paxCount++
            tvPaxCount.text = paxCount.toString()
        }

        // Handle Image Upload
        btnUploadImage.setOnClickListener { selectImages() }

        // Save Button
        btnSaveRoom.setOnClickListener {
            val description = etRoomDescription.text.toString()
            val price = etRoomPrice.text.toString()

            if (selectedCategory != null && description.isNotEmpty() && price.isNotEmpty()) {
                Toast.makeText(requireContext(), "Room Saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun fetchCategoriesFromFirebase() {
        databaseReference.child("categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categories.clear()
                for (child in snapshot.children) {
                    val category = child.getValue(String::class.java)
                    if (category != null) {
                        categories.add(category)
                    }
                }
                setupCategoryRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAmenitiesFromFirebase() {
        databaseReference.child("amenities").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                amenities.clear()
                for (child in snapshot.children) {
                    val amenity = child.getValue(String::class.java)
                    if (amenity != null) {
                        amenities.add(amenity)
                    }
                }
                setupAmenitiesRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch amenities: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCategoryRecyclerView() {
        rvCategory.layoutManager = LinearLayoutManager(requireContext())
        rvCategory.adapter = CustomRoomCategoryAdapter(categories) { selected ->
            selectedCategory = selected
        }
    }

    private fun setupAmenitiesRecyclerView() {
        rvAmenities.layoutManager = LinearLayoutManager(requireContext())
        rvAmenities.adapter = CustomRoomAmenitiesAdapter(amenities) { selected ->
            selectedAmenities.clear() // Clear and update the selected amenities
            selectedAmenities.addAll(selected)
        }
    }


    private fun selectImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        imagePicker.launch(intent)
    }

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                data?.clipData?.let {
                    for (i in 0 until it.itemCount) {
                        selectedImages.add(it.getItemAt(i).uri)
                    }
                } ?: data?.data?.let {
                    selectedImages.add(it)
                }
                updateImagePreview()
            }
        }

    private fun updateImagePreview() {
        imagePreviewContainer.removeAllViews()
        selectedImages.forEach { uri ->
            val imageView = ImageView(requireContext()).apply {
                setImageURI(uri)
                layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    setMargins(8, 8, 8, 8)
                }
            }
            imagePreviewContainer.addView(imageView)
        }
    }
}
