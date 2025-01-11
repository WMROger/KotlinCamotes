package com.example.kotlinactivities.adminPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminPage.ViewModel.CategoryViewModel
import com.example.kotlinactivities.adminadapter.RoomCarouselAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class AddRoomFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var fabAddRoom: FloatingActionButton
    private val categoryViewModel: CategoryViewModel by activityViewModels() // Shared ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_room, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.roomsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        // Initialize TabLayout
        tabLayout = view.findViewById(R.id.tabLayoutRooms)

        // Observe ViewModel categories
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            updateTabLayout(categories)
        }

        // Initialize Floating Action Button
        fabAddRoom = view.findViewById(R.id.fabAddRoom)
        fabAddRoom.setOnClickListener {
            val bottomSheet = AddRoomBottomSheetFragment { option ->
                when (option) {
                    AddRoomBottomSheetFragment.Option.ADD_CATEGORY -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, AddCategoryFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                    AddRoomBottomSheetFragment.Option.ADD_ROOM -> {
                        Toast.makeText(context, "Add Room Clicked", Toast.LENGTH_SHORT).show()
                    }
                    AddRoomBottomSheetFragment.Option.ADD_AMENITIES -> {
                        Toast.makeText(context, "Add Amenities Clicked", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        return view
    }

    private fun updateTabLayout(categories: List<String>) {
        tabLayout.removeAllTabs()
        for (category in categories) {
            tabLayout.addTab(tabLayout.newTab().setText(category))
        }

        // Set adapter data based on the first tab by default
        if (categories.isNotEmpty()) {
            val initialCategory = categories.first()
            val roomAdapter = RoomAdapter(getSampleRooms(initialCategory))
            recyclerView.adapter = roomAdapter
        }

        // Handle Tab Selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val roomCategory = tab?.text.toString()
                val updatedRooms = getSampleRooms(roomCategory)
                (recyclerView.adapter as? RoomAdapter)?.updateRooms(updatedRooms)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun getSampleRooms(category: String): List<Room> {
        return when (category) {
            "Deluxe Room" -> listOf(
                Room("Cupid's Deluxe Room", 4.9, 5, "₱1,678/night", R.drawable.ic_cupids_deluxe)
            )
            "Barkada Room" -> listOf(
                Room("Tropical Barkada Room", 4.5, 8, "₱2,500/night", R.drawable.ic_cupids_deluxe)
            )
            "Regular Room" -> listOf(
                Room("Cozy Regular Room", 4.0, 3, "₱1,000/night", R.drawable.ic_cupids_deluxe)
            )
            else -> emptyList()
        }
    }

    data class Room(val name: String, val rating: Double, val maxPerson: Int, val price: String, val imageResId: Int)

    class RoomAdapter(private var rooms: List<Room>) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.room_card, parent, false)
            return RoomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
            holder.bind(rooms[position])
        }

        override fun getItemCount(): Int = rooms.size

        fun updateRooms(updatedRooms: List<Room>) {
            rooms = updatedRooms
            notifyDataSetChanged()
        }

        class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val roomCarousel: ViewPager2 = itemView.findViewById(R.id.roomCarousel)
            private val roomName: TextView = itemView.findViewById(R.id.roomTitle)
            private val roomRating: TextView = itemView.findViewById(R.id.roomRating)
            private val roomMaxPerson: TextView = itemView.findViewById(R.id.roomPeople)
            private val roomPrice: TextView = itemView.findViewById(R.id.roomPrice)

            fun bind(room: Room) {
                roomCarousel.adapter = RoomCarouselAdapter(listOf(room.imageResId))
                roomName.text = room.name
                roomRating.text = "${room.rating} ★"
                roomMaxPerson.text = "People: ${room.maxPerson}"
                roomPrice.text = room.price
            }
        }
    }
}
