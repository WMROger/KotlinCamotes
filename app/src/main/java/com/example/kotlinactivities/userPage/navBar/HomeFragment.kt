package com.example.kotlinactivities.userPage.navBar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.RoomAdapter
import com.example.kotlinactivities.userPage.homePage.RoomDetailsActivity
import com.example.kotlinactivities.model.Room
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    private lateinit var filtersLayout: LinearLayoutCompat
    private lateinit var roomsRecyclerView: RecyclerView
    private lateinit var roomAdapter: RoomAdapter
    private val roomList = mutableListOf<Room>() // List of all rooms
    private val originalRoomList = mutableListOf<Room>() // Original unfiltered data
    private val categories = mutableListOf<String>() // To store categories from Firebase
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        filtersLayout = view.findViewById(R.id.filtersLayout)
        roomsRecyclerView = view.findViewById(R.id.roomsRecyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // Set up RecyclerView
        roomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        roomAdapter = RoomAdapter(
            roomList,
            onDeleteClick = { room ->
                Toast.makeText(requireContext(), "${room.title} deleted!", Toast.LENGTH_SHORT).show()
            },
            onRoomClick = { room ->
                navigateToRoomDetails(room)
            },
            isMyRoomsContext = false
        )
        roomsRecyclerView.adapter = roomAdapter

        // Fetch categories from Firebase
        fetchCategoriesFromFirebase()

        // Load room data
        loadRoomData()

        // Set up SwipeRefreshLayout
        setupSwipeRefresh()

        return view
    }

    private fun fetchCategoriesFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("categories")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categories.clear() // Clear existing categories
                filtersLayout.removeAllViews() // Clear existing filter buttons
                for (child in snapshot.children) {
                    val category = child.getValue(String::class.java)
                    if (category != null) {
                        categories.add(category)
                        addFilterButton(category) // Dynamically add a filter button
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addFilterButton(category: String) {
        val filterButton = TextView(requireContext()).apply {
            text = category
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (category == selectedCategory) Color.WHITE else Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(16, 8, 16, 8)
            background = if (category == selectedCategory) {
                requireContext().getDrawable(R.drawable.filter_button_selected)
            } else {
                requireContext().getDrawable(R.drawable.filter_button_default)
            }
            layoutParams = LinearLayoutCompat.LayoutParams(
                0,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                1f
            )

            setOnClickListener {
                updateRoomList(category) // Apply or reset filter
                resetFilterButtons() // Reset visual styles for all buttons
                // Highlight this button if it's now the selected category
                if (selectedCategory == category) {
                    background = requireContext().getDrawable(R.drawable.filter_button_selected)
                    setTextColor(Color.WHITE)
                }
            }
        }

        filtersLayout.addView(filterButton)
    }

    private fun resetFilterButtons() {
        for (i in 0 until filtersLayout.childCount) {
            val child = filtersLayout.getChildAt(i) as TextView
            val isSelected = child.text.toString() == selectedCategory
            child.background = if (isSelected) {
                requireContext().getDrawable(R.drawable.filter_button_selected)
            } else {
                requireContext().getDrawable(R.drawable.filter_button_default)
            }
            child.setTextColor(if (isSelected) Color.WHITE else Color.BLACK)
        }
    }

    private fun updateRoomList(category: String?) {
        if (selectedCategory == category) {
            // If the selected filter is clicked again, reset to show all rooms
            selectedCategory = null // Reset selected category
            roomList.clear()
            roomList.addAll(originalRoomList) // Show all rooms
            roomAdapter.notifyDataSetChanged()
            resetFilterButtons() // Reset all filter button styles
            Toast.makeText(requireContext(), "Filter reset. Showing all rooms.", Toast.LENGTH_SHORT).show()
            return
        }

        // Apply new category filter
        selectedCategory = category
        val filteredRooms = if (category == null || category == "All") {
            originalRoomList // Show all rooms if "All" is selected
        } else {
            originalRoomList.filter { it.roomCategory.equals(category, ignoreCase = true) }
        }

        roomList.clear()
        roomList.addAll(filteredRooms)
        roomAdapter.notifyDataSetChanged()

        if (filteredRooms.isEmpty()) {
            Toast.makeText(requireContext(), "No rooms available for category: $category", Toast.LENGTH_SHORT).show()
        }

        Log.d("HomeFragment", "Filtered rooms: ${filteredRooms.size} for category: $category")
    }

    private fun loadRoomData() {
        val roomsRef = FirebaseDatabase.getInstance().getReference("rooms")

        roomsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                roomList.clear()
                originalRoomList.clear()

                for (child in snapshot.children) {
                    val id = child.key
                    val title = child.child("description").getValue(String::class.java) ?: "Unknown Room"
                    val pax = child.child("pax").getValue(Int::class.java) ?: 0
                    val price = child.child("price").getValue(String::class.java) ?: "N/A"
                    val imageUrl = child.child("image_url").getValue(String::class.java) ?: ""
                    val imageUrls = child.child("image_urls").getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf()
                    val bookingStatus = child.child("bookingStatus").getValue(String::class.java) ?: "Available"
                    val roomCategory = child.child("category").getValue(String::class.java) ?: "Unknown Category" // Fetch roomCategory
                    val isFavorited = child.child("isFavorited").getValue(Boolean::class.java) ?: false
                    val rating = "${(3..5).random()}.${(0..9).random()} ★" // Mock random ratings

                    // Add to the room list
                    roomList.add(
                        Room(
                            id = id,
                            imageUrl = imageUrl,
                            imageUrls = imageUrls,
                            title = title,
                            people = "$pax People",
                            price = "₱$price/night",
                            rating = rating,
                            bookingStatus = bookingStatus,
                            roomCategory = roomCategory, // Add roomCategory here
                            isFavorited = isFavorited
                        )
                    )
                }

                // Save the original data and update the RecyclerView
                originalRoomList.addAll(roomList)
                roomAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load rooms: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToRoomDetails(room: Room) {
        val intent = Intent(requireContext(), RoomDetailsActivity::class.java).apply {
            putExtra("room", room)
        }
        startActivity(intent)
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            Toast.makeText(requireContext(), "Refreshing rooms...", Toast.LENGTH_SHORT).show()
            selectedCategory = null // Reset the selected category
            loadRoomData() // Reload room data
            resetFilterButtons() // Reset the visual state of filters
            swipeRefreshLayout.isRefreshing = false // Stop refresh animation
        }
    }

}
