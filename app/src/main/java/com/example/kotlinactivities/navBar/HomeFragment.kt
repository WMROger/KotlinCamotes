package com.example.kotlinactivities.navBar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.RoomAdapter
import com.example.kotlinactivities.homePage.RoomDetailsActivity
import com.example.kotlinactivities.model.Room

class HomeFragment : Fragment() {

    private lateinit var roomsRecyclerView: RecyclerView
    private lateinit var roomAdapter: RoomAdapter
    private val roomList = mutableListOf<Room>() // Mutable list for updating
    private val originalRoomList = mutableListOf<Room>() // Keeps original unfiltered data

    private lateinit var deluxeRoomFilter: TextView
    private lateinit var barkadaRoomFilter: TextView
    private lateinit var regularRoomFilter: TextView

    private var selectedFilter: String? = null // Tracks the currently selected filter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Find filter buttons
        deluxeRoomFilter = view.findViewById(R.id.deluxeRoomFilter)
        barkadaRoomFilter = view.findViewById(R.id.barkadaRoomFilter)
        regularRoomFilter = view.findViewById(R.id.regularRoomFilter)

        // Setup RecyclerView
        roomsRecyclerView = view.findViewById(R.id.roomsRecyclerView)
        roomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize RoomAdapter with click handling
        roomAdapter = RoomAdapter(roomList) { room ->
            navigateToRoomDetails(room)
        }
        roomsRecyclerView.adapter = roomAdapter

        // Load room data
        loadRoomData()

        // Setup filter button functionality
        setupFilters()

        return view
    }

    private fun loadRoomData() {
        roomList.apply {
            add(
                Room(
                    imageUrl = R.drawable.ic_cupids_deluxe,
                    title = "Cupid's Deluxe Room",
                    people = "2",
                    price = "₱1,678/night",
                    rating = "4.9 ★"
                )
            )
            add(
                Room(
                    imageUrl = R.drawable.ic_cupids_deluxe,
                    title = "Barkada Room",
                    people = "5",
                    price = "₱2,500/night",
                    rating = "4.8 ★"
                )
            )
            add(
                Room(
                    imageUrl = R.drawable.ic_cupids_deluxe,
                    title = "Regular Room",
                    people = "3",
                    price = "₱1,200/night",
                    rating = "4.5 ★"
                )
            )
        }

        // Copy all rooms to the original list
        originalRoomList.addAll(roomList)

        Log.d("HomeFragment", "Room data loaded: ${roomList.size} items")
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
                // If already selected, reset to default state
                resetFilters()
                updateRoomList(null) // Show all rooms
            } else {
                resetFilters()
                deluxeRoomFilter.setBackgroundResource(R.drawable.filter_button_selected) // Highlight green
                deluxeRoomFilter.setTextColor(resources.getColor(R.color.white, null)) // Keep text white for selected
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
                barkadaRoomFilter.setTextColor(resources.getColor(R.color.white, null)) // White text for selected
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
                regularRoomFilter.setTextColor(resources.getColor(R.color.white, null)) // White text for selected
                selectedFilter = "Regular"
                updateRoomList("Regular")
            }
        }
    }

    private fun updateRoomList(filter: String?) {
        // Reset to the original list if no filter is applied
        val filteredRooms = if (filter == null) {
            originalRoomList
        } else {
            originalRoomList.filter { it.title.contains(filter, ignoreCase = true) }
        }

        // Update adapter with filtered data
        roomAdapter.updateRooms(filteredRooms)
    }

    private fun navigateToRoomDetails(room: Room) {
        // Use an Intent to navigate to RoomDetailsActivity
        val intent = Intent(requireContext(), RoomDetailsActivity::class.java).apply {
            putExtra("room", room)
        }
        startActivity(intent)
    }
}
