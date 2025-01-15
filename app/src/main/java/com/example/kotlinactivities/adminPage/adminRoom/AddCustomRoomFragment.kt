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
import com.example.kotlinactivities.adminPage.adminAdapter.customRoomAdapter.CustomRoomAmenitiesAdapter
import com.example.kotlinactivities.adminPage.adminAdapter.customRoomAdapter.CustomRoomCategoryAdapter
import com.example.kotlinactivities.adminPage.adminAdapter.customRoomAdapter.SingleSelectionCategoryAdapter
import com.example.kotlinactivities.adminPage.upload.uploadImageAndSaveToRealtimeDB // Import the separate function
import com.google.firebase.database.FirebaseDatabase

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
    private val categories = mutableListOf<String>() // Categories from Realtime DB
    private val amenities = mutableListOf<String>() // Amenities from Realtime DB
    private var selectedCategory: String? = null // Selected Category
    private val selectedAmenities = mutableSetOf<String>() // Selected Amenities

    private val databaseReference = FirebaseDatabase.getInstance().reference // Firebase DB reference

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

        // Fetch data
        fetchCategoriesFromRealtimeDatabase()
        fetchAmenitiesFromRealtimeDatabase()

        // Adjust pax count
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

        // Image upload
        btnUploadImage.setOnClickListener { selectImages() }

        // Save room details
        btnSaveRoom.setOnClickListener {
            val description = etRoomDescription.text.toString()
            val price = etRoomPrice.text.toString()

            if (selectedCategory != null && description.isNotEmpty() && price.isNotEmpty()) {
                if (selectedImages.isNotEmpty()) {
                    uploadImageAndSaveToRealtimeDB(
                        selectedImages[0], // Use the first selected image
                        requireContext(),
                        mapOf(
                            "category" to selectedCategory,
                            "description" to description,
                            "price" to price,
                            "pax" to paxCount,
                            "amenities" to selectedAmenities.toList()
                        )
                    ) { success ->
                        if (success) {
                            // Reset fields after successful upload
                            resetFields()
                        } else {
                            Toast.makeText(requireContext(), "Failed to save room details.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Please upload an image.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }



        return view
    }
    private fun resetFields() {
        selectedImages.clear() // Clear the selected images list
        selectedCategory = null // Reset the selected category
        selectedAmenities.clear() // Clear the selected amenities
        etRoomDescription.text.clear() // Clear the room description input
        etRoomPrice.text.clear() // Clear the room price input
        tvPaxCount.text = "5" // Reset pax count to default value
        paxCount = 5 // Reset pax count variable
        imagePreviewContainer.removeAllViews() // Clear the image preview container
        Toast.makeText(requireContext(), "Fields reset successfully.", Toast.LENGTH_SHORT).show()
    }

    private fun fetchCategoriesFromRealtimeDatabase() {
        databaseReference.child("categories").get().addOnSuccessListener { snapshot ->
            categories.clear()
            snapshot.children.mapNotNullTo(categories) { it.getValue(String::class.java) }
            setupCategoryRecyclerView()
        }
    }

    private fun fetchAmenitiesFromRealtimeDatabase() {
        databaseReference.child("amenities").get().addOnSuccessListener { snapshot ->
            amenities.clear()
            snapshot.children.mapNotNullTo(amenities) { it.getValue(String::class.java) }
            setupAmenitiesRecyclerView()
        }
    }

    private fun setupCategoryRecyclerView() {
        rvCategory.layoutManager = LinearLayoutManager(requireContext())
        rvCategory.adapter = SingleSelectionCategoryAdapter(categories) { selected ->
            selectedCategory = selected // Store the selected category
        }
    }


    private fun setupAmenitiesRecyclerView() {
        rvAmenities.layoutManager = LinearLayoutManager(requireContext())
        rvAmenities.adapter = CustomRoomAmenitiesAdapter(amenities) { selected ->
            selectedAmenities.clear()
            selectedAmenities.addAll(selected)
        }
    }

    private fun selectImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        imagePicker.launch(intent)
    }

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { selectedImages.add(it) }
                updateImagePreview()
            }
        }

    private fun updateImagePreview() {
        imagePreviewContainer.removeAllViews()
        selectedImages.forEachIndexed { index, uri ->
            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
            }

            val fileNameView = TextView(requireContext()).apply {
                text = uri.lastPathSegment ?: "File"
                textSize = 14f
                setOnClickListener { showZoomedImageDialog(uri, index) }
            }

            container.addView(fileNameView)
            imagePreviewContainer.addView(container)
        }
    }

    private fun showZoomedImageDialog(imageUri: Uri, position: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_zoomed_image, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.zoomedImageView)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        imageView.setImageURI(imageUri)

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnDelete.setOnClickListener {
            selectedImages.removeAt(position)
            updateImagePreview()
            dialog.dismiss()
        }

        dialog.show()
    }
}
