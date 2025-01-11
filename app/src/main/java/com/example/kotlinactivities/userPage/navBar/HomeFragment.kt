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

class HomeFragment : Fragment() {

    private lateinit var roomsRecyclerView: RecyclerView
    private lateinit var roomAdapter: RoomAdapter
    private val roomList = mutableListOf<Room>() // Mutable list for updating
    private val originalRoomList = mutableListOf<Room>() // Keeps original unfiltered data

    private lateinit var deluxeRoomFilter: TextView
    private lateinit var barkadaRoomFilter: TextView
    private lateinit var regularRoomFilter: TextView

    private lateinit var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    private var selectedFilter: String? = null // Tracks the currently selected filter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Find views
        deluxeRoomFilter = view.findViewById(R.id.deluxeRoomFilter)
        barkadaRoomFilter = view.findViewById(R.id.barkadaRoomFilter)
        regularRoomFilter = view.findViewById(R.id.regularRoomFilter)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        roomsRecyclerView = view.findViewById(R.id.roomsRecyclerView)

        // Setup RecyclerView
        roomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        roomAdapter = RoomAdapter(
            roomList,
            onDeleteClick = { room ->
                // Handle favorite logic or other functionality
                Toast.makeText(requireContext(), "${room.title} deleted!", Toast.LENGTH_SHORT).show()
            },
            onRoomClick = { room ->
                navigateToRoomDetails(room) // Navigate to RoomDetailsActivity
            },
            isMyRoomsContext = false
        )
        roomsRecyclerView.adapter = roomAdapter

        // Setup SwipeRefreshLayout
        setupSwipeRefresh()

        // Load room data
        loadRoomData()

        // Setup filter button functionality
        setupFilters()

        return view
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            Toast.makeText(requireContext(), "Refreshing rooms...", Toast.LENGTH_SHORT).show()
            loadRoomData() // Reload room data
            swipeRefreshLayout.isRefreshing = false // Stop refresh animation
        }
    }

    private fun loadRoomData() {
        // Clear existing data to reload
        roomList.clear()
        originalRoomList.clear()

        // Add mock data (replace with real API data later)
        roomList.addAll(
            listOf(
                Room(
                    imageUrls = listOf(
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png",
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe2.png",
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png",
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe2.png"
                    ),
                    title = "Cupid's Deluxe Room",
                    people = "2",
                    price = "₱1,678/night",
                    rating = "4.9 ★"
                ),
                Room(
                    imageUrls = listOf(
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe2.png",
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png",
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe2.png",
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png"
                    ),
                    title = "Barkada Room",
                    people = "5",
                    price = "₱2,500/night",
                    rating = "4.8 ★"
                ),
                Room(
                    imageUrls = listOf(
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png",
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png",
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe2.png",
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe2.png",
                        "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe2.png"
                    ),
                    title = "Regular Room",
                    people = "3",
                    price = "₱1,200/night",
                    rating = "4.5 ★"
                )
            )
        )
        originalRoomList.addAll(roomList) // Keep original data for filtering
        roomAdapter.notifyDataSetChanged()
    }

    private fun setupFilters() {
        // Group all filter buttons
        val filters = listOf(deluxeRoomFilter, barkadaRoomFilter, regularRoomFilter)

        // Reset all buttons to default state
        fun resetFilters() {
            filters.forEach { filter ->
                filter.setBackgroundResource(R.drawable.filter_button_default) // Default black-and-white background
                filter.setTextColor(resources.getColor(R.color.black, null)) // Default black text color for unselected buttons
            }
            selectedFilter = null // Clear the selected filter
        }

        // Set click listeners for each filter
        deluxeRoomFilter.setOnClickListener {
            if (selectedFilter == "Deluxe") {
                resetFilters()
                updateRoomList(null) // Show all rooms
            } else {
                resetFilters()
                deluxeRoomFilter.setBackgroundResource(R.drawable.filter_button_selected) // Highlight green
                deluxeRoomFilter.setTextColor(resources.getColor(R.color.white, null))
                selectedFilter = "Deluxe"
                updateRoomList("Deluxe") // Show only Deluxe Rooms
            }
        }

        barkadaRoomFilter.setOnClickListener {
            if (selectedFilter == "Barkada") {
                resetFilters()
                updateRoomList(null)
            } else {
                resetFilters()
                barkadaRoomFilter.setBackgroundResource(R.drawable.filter_button_selected)
                barkadaRoomFilter.setTextColor(resources.getColor(R.color.white, null))
                selectedFilter = "Barkada"
                updateRoomList("Barkada")
            }
        }

        regularRoomFilter.setOnClickListener {
            if (selectedFilter == "Regular") {
                resetFilters()
                updateRoomList(null)
            } else {
                resetFilters()
                regularRoomFilter.setBackgroundResource(R.drawable.filter_button_selected)
                regularRoomFilter.setTextColor(resources.getColor(R.color.white, null))
                selectedFilter = "Regular"
                updateRoomList("Regular")
            }
        }
    }

    private fun updateRoomList(filter: String?) {
        val filteredRooms = if (filter == null) {
            originalRoomList
        } else {
            originalRoomList.filter { it.title.contains(filter, ignoreCase = true) }
        }

        // Update adapter with filtered data
        roomAdapter.updateRooms(filteredRooms)
    }

    private fun navigateToRoomDetails(room: Room) {
        val firstImageUrl = room.imageUrls?.firstOrNull() // Get the first URL or null
            ?: "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png" // Default image if empty

        val intent = Intent(requireContext(), RoomDetailsActivity::class.java).apply {
            putExtra("room", room) // Pass the entire Room object
            putExtra("firstImageUrl", firstImageUrl) // Pass the first URL explicitly
            putExtra("isFromMyRoom", false) // Indicate that navigation is not from My Room
        }
        startActivity(intent)
    }

}
