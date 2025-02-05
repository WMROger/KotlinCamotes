package com.example.kotlinactivities.adminPage.adminRoom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminPage.adminAdapter.AdminRoomAdapter
import com.example.kotlinactivities.adminPage.adminAdapter.RoomCarouselAdapter
import com.example.kotlinactivities.model.AdminRoom
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*

class AddRoomFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var fabAddRoom: FloatingActionButton
    private lateinit var databaseReference: DatabaseReference // Firebase Database Reference
    private val categories = mutableListOf<String>() // Store fetched categories locally

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_room, container, false)

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("categories")

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.roomsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        // Initialize TabLayout
        tabLayout = view.findViewById(R.id.tabLayoutRooms)

        // Fetch categories from Firebase and display them in TabLayout
        fetchCategoriesFromFirebase()

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
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, AddCustomRoomFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                    AddRoomBottomSheetFragment.Option.ADD_AMENITIES -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, AddAmenitiesFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        return view
    }

    // Fetch categories from Firebase
    private fun fetchCategoriesFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categories.clear()
                tabLayout.removeAllTabs()

                for (child in snapshot.children) {
                    val category = child.getValue(String::class.java)
                    if (category != null) {
                        categories.add(category)
                        tabLayout.addTab(tabLayout.newTab().setText(category))
                    }
                }

                if (categories.isNotEmpty()) {
                    // Fetch rooms for the first category by default
                    fetchRoomsByCategory(categories.first())
                }

                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        val selectedCategory = tab?.text.toString()
                        fetchRoomsByCategory(selectedCategory) // Fetch for the selected tab
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fetch rooms by category
    private fun fetchRoomsByCategory(category: String) {
        val roomsRef = FirebaseDatabase.getInstance().getReference("rooms")

        roomsRef.orderByChild("category").equalTo(category)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roomList = mutableListOf<AdminRoom>()

                    for (child in snapshot.children) {
                        val roomId = child.key ?: "" // Get the roomId from the Firebase snapshot key
                        val roomName = child.child("description").getValue(String::class.java) ?: "Unknown Room"
                        val roomRating = (3..4).random() + (0..9).random() / 4.0 // Mock random ratings
                        val maxPerson = child.child("pax").getValue(Int::class.java) ?: 0
                        val price = "₱${child.child("price").getValue(String::class.java) ?: "N/A"}"
                        val imageUrl = child.child("image_url").getValue(String::class.java) ?: ""

                        roomList.add(AdminRoom(roomName, roomRating, maxPerson, price, imageUrl, roomId))
                    }

                    // Populate the RecyclerView with fetched rooms
                    if (recyclerView.adapter == null) {
                        recyclerView.adapter = AdminRoomAdapter(roomList)
                    } else {
                        (recyclerView.adapter as? AdminRoomAdapter)?.updateRooms(roomList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to fetch rooms: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Function to delete the room from Firebase database
    private fun deleteRoomFromDatabase(roomId: String) {
        val roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId)
        roomRef.removeValue().addOnSuccessListener {
            Toast.makeText(context, "Room deleted successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to delete room: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
