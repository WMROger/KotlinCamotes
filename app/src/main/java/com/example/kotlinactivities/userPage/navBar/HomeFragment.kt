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
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    private lateinit var filtersLayout: LinearLayoutCompat
    private lateinit var roomsRecyclerView: RecyclerView
    private lateinit var roomAdapter: RoomAdapter
    private val roomList = mutableListOf<Room>() // List of all rooms
    private val originalRoomList = mutableListOf<Room>() // Original unfiltered data
    private val categories = mutableListOf<String>() // To store categories from Firebase

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
        // Create a TextView for the category
        val filterButton = TextView(requireContext()).apply {
            text = category
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(16, 8, 16, 8)
            background = requireContext().getDrawable(R.drawable.filter_button_default)
            layoutParams = LinearLayoutCompat.LayoutParams(
                0,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                1f // Equal weight
            )

            // Handle click events
            setOnClickListener {
                updateRoomList(category) // Filter the RecyclerView based on the selected category
                resetFilterButtons()
                background = requireContext().getDrawable(R.drawable.filter_button_selected) // Highlight selected filter
                setTextColor(Color.WHITE) // Change text color for selected filter
            }
        }

        // Add the TextView to the filters layout
        filtersLayout.addView(filterButton)
    }

    private fun resetFilterButtons() {
        for (i in 0 until filtersLayout.childCount) {
            val child = filtersLayout.getChildAt(i) as TextView
            child.background = requireContext().getDrawable(R.drawable.filter_button_default) // Reset background
            child.setTextColor(Color.BLACK) // Reset text color
        }
    }

    private fun updateRoomList(category: String?) {
        val filteredRooms = if (category == null || category == "All") {
            originalRoomList // Show all rooms
        } else {
            originalRoomList.filter { it.title.contains(category, ignoreCase = true) }
        }

        // Update RecyclerView
        roomList.clear()
        roomList.addAll(filteredRooms)
        roomAdapter.notifyDataSetChanged()
    }

    private fun loadRoomData() {
        // Add mock data for now (replace with real API data later)
        roomList.addAll(
            listOf(
                Room(
                    imageUrls = listOf("https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png"),
                    title = "Deluxe Room",
                    people = "2",
                    price = "₱1,678/night",
                    rating = "4.9 ★"
                ),
                Room(
                    imageUrls = listOf("https://waveaway.scarlet2.io/assets/ic_cupids_deluxe2.png"),
                    title = "Barkada Room",
                    people = "5",
                    price = "₱2,500/night",
                    rating = "4.8 ★"
                ),
                Room(
                    imageUrls = listOf("https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png"),
                    title = "Regular Room",
                    people = "3",
                    price = "₱1,200/night",
                    rating = "4.5 ★"
                )
            )
        )
        originalRoomList.addAll(roomList) // Keep original data
        roomAdapter.notifyDataSetChanged()
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
            loadRoomData() // Reload room data
            swipeRefreshLayout.isRefreshing = false // Stop refresh animation
        }
    }
}
