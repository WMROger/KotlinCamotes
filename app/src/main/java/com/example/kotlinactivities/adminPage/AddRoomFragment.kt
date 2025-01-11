package com.example.kotlinactivities.adminPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminadapter.RoomCarouselAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class AddRoomFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var fabAddRoom: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_room, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.roomsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        // Initialize TabLayout
        tabLayout = view.findViewById(R.id.tabLayoutRooms)

        // Add tabs to TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Deluxe Room"))
        tabLayout.addTab(tabLayout.newTab().setText("Barkada Room"))
        tabLayout.addTab(tabLayout.newTab().setText("Regular Room"))

        // Set adapter with sample data for Deluxe Room by default
        val sampleRooms = getSampleRooms("Deluxe Room")
        val roomAdapter = RoomAdapter(sampleRooms)
        recyclerView.adapter = roomAdapter

        // Handle tab switching
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val roomCategory = tab?.text.toString()
                val updatedRooms = getSampleRooms(roomCategory)
                roomAdapter.updateRooms(updatedRooms)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Initialize Floating Action Button
        fabAddRoom = view.findViewById(R.id.fabAddRoom)
        fabAddRoom.setOnClickListener {
            // Navigate to Add Room Form (Add your own logic here)
        }
        // Initialize Floating Action Button
        fabAddRoom = view.findViewById(R.id.fabAddRoom)
        fabAddRoom.setOnClickListener {
            // Show the bottom sheet
            val bottomSheet = AddRoomBottomSheetFragment { option ->
                when (option) {
                    AddRoomBottomSheetFragment.Option.ADD_CATEGORY -> {
                        // Redirect to AddCategoryFragment
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, AddCategoryFragment()) // Replace with your container ID
                            .addToBackStack(null) // Add this transaction to the back stack
                            .commit()
                    }
                    AddRoomBottomSheetFragment.Option.ADD_ROOM -> {
                        // Handle Add Room Action
                        Toast.makeText(context, "Add Room Clicked", Toast.LENGTH_SHORT).show()
                    }
                    AddRoomBottomSheetFragment.Option.ADD_AMENITIES -> {
                        // Handle Add Amenities Action
                        Toast.makeText(context, "Add Amenities Clicked", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        return view
    }

    // Sample data generation for different room categories
    private fun getSampleRooms(category: String): List<Room> {
        return when (category) {
            "Deluxe Room" -> listOf(
                Room(
                    name = "Cupid's Deluxe Room",
                    rating = 4.9,
                    maxPerson = 5,
                    price = "₱1,678/night",
                    imageResId = R.drawable.ic_cupids_deluxe
                )
            )
            "Barkada Room" -> listOf(
                Room(
                    name = "Tropical Barkada Room",
                    rating = 4.5,
                    maxPerson = 8,
                    price = "₱2,500/night",
                    imageResId = R.drawable.ic_cupids_deluxe // Replace with another image
                )
            )
            "Regular Room" -> listOf(
                Room(
                    name = "Cozy Regular Room",
                    rating = 4.0,
                    maxPerson = 3,
                    price = "₱1,000/night",
                    imageResId = R.drawable.ic_cupids_deluxe // Replace with another image
                )
            )
            else -> emptyList()
        }
    }

    // Room data class
    data class Room(
        val name: String,
        val rating: Double,
        val maxPerson: Int,
        val price: String,
        val imageResId: Int
    )

    // Adapter for RecyclerView
    class RoomAdapter(private var rooms: List<Room>) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.room_card, parent, false)
            return RoomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
            val room = rooms[position]
            holder.bind(room)
        }

        override fun getItemCount(): Int = rooms.size

        fun updateRooms(updatedRooms: List<Room>) {
            rooms = updatedRooms
            notifyDataSetChanged()
        }

        class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val roomCarousel: ViewPager2 = itemView.findViewById(R.id.roomCarousel) // Correct type is ViewPager2
            private val roomName: TextView = itemView.findViewById(R.id.roomTitle)
            private val roomRating: TextView = itemView.findViewById(R.id.roomRating)
            private val roomMaxPerson: TextView = itemView.findViewById(R.id.roomPeople)
            private val roomPrice: TextView = itemView.findViewById(R.id.roomPrice)

            fun bind(room: Room) {
                // Set up ViewPager2 with images
                val carouselAdapter = RoomCarouselAdapter(listOf(room.imageResId)) // Pass the list of images for the carousel
                roomCarousel.adapter = carouselAdapter

                // Set room details
                roomName.text = room.name
                roomRating.text = "${room.rating} ★"
                roomMaxPerson.text = "People: ${room.maxPerson}"
                roomPrice.text = room.price
            }
        }

    }
}
