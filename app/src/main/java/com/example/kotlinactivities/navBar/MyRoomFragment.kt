package com.example.kotlinactivities.navBar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.MyRoomsAdapter
import com.example.kotlinactivities.homePage.RoomDetailsActivity
import com.example.kotlinactivities.model.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyRoomFragment : Fragment() {

    private lateinit var myRoomsRecyclerView: RecyclerView
    private lateinit var myRoomsAdapter: MyRoomsAdapter
    private val myRoomsList = mutableListOf<Room>()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_room, container, false)

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("bookings")

        // Initialize RecyclerView
        myRoomsRecyclerView = view.findViewById(R.id.myRoomsRecyclerView)
        myRoomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        myRoomsAdapter = MyRoomsAdapter(
            myRoomsList,
            onFavoriteClicked = { room -> handleFavoriteClick(room) },
            onDeleteClicked = { room -> handleDeleteClick(room) } // Add delete click handler
        )

        myRoomsRecyclerView.adapter = myRoomsAdapter

        // Load Rooms from Firebase
        loadRoomsFromFirebase()

        return view
    }

    private fun loadRoomsFromFirebase() {
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId != null) {
            // Query the bookings for the current user
            databaseReference.orderByChild("userId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("MyRoomFragment", "Snapshot value: ${snapshot.value}") // Log the raw snapshot data
                        myRoomsList.clear()
                        for (roomSnapshot in snapshot.children) {
                            Log.d("MyRoomFragment", "Room snapshot: ${roomSnapshot.value}") // Log each child node
                            val roomId = roomSnapshot.key // Get the unique room ID (key)
                            val roomTitle = roomSnapshot.child("roomTitle").value as? String ?: "No Title"
                            val totalPrice = roomSnapshot.child("totalPrice").value as? String ?: "₱0"
                            val guestCount = roomSnapshot.child("guestCount").value as? Long ?: 1L
                            val imageUrl = roomSnapshot.child("imageUrl").value as? String
                                ?: "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png" // Default placeholder image URL
                            val rating = "4.9" // Replace with dynamic rating if available
                            val bookingStatus = roomSnapshot.child("status").value as? String ?: "Pending" // Retrieve booking status

                            // Add the room to the list
                            myRoomsList.add(
                                Room(
                                    id = roomId, // Save the room ID for deletion
                                    imageUrl = imageUrl,
                                    title = roomTitle,
                                    people = "$guestCount People",
                                    price = totalPrice,
                                    rating = rating,
                                    isFavorited = false
                                )
                            )

                            // Navigate to room details on click
                            myRoomsAdapter.setOnItemClickListener { room ->
                                navigateToRoomDetails(room, bookingStatus) // Pass the booking status
                            }
                        }

                        // Notify adapter about the data change
                        myRoomsAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MyRoomFragment", "Failed to load rooms: ${error.message}")
                    }
                })
        } else {
            Log.e("MyRoomFragment", "User is not logged in.")
        }
    }

    private fun navigateToRoomDetails(room: Room, bookingStatus: String) {
        val intent = Intent(requireContext(), RoomDetailsActivity::class.java).apply {
            putExtra("room", room)
            putExtra("isFromMyRoom", true) // Indicate that navigation is from MyRoomFragment
            putExtra("bookingStatus", bookingStatus) // Pass the booking status
        }
        startActivity(intent)
    }

    private fun handleFavoriteClick(room: Room) {
        room.isFavorited = !room.isFavorited // Toggle favorite state
        myRoomsAdapter.notifyDataSetChanged() // Refresh the RecyclerView
        // Add logic to update the favorite state in the database if necessary
        Log.d("MyRoomFragment", "Favorite clicked for room: ${room.title}, state: ${room.isFavorited}")
    }

    private fun handleDeleteClick(room: Room) {
        val roomId = room.id
        if (roomId != null) {
            // Delete the room from Firebase
            databaseReference.child(roomId).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("MyRoomFragment", "Room deleted successfully: $roomId")
                        myRoomsAdapter.removeRoom(room) // Remove the room from the list
                    } else {
                        Log.e("MyRoomFragment", "Failed to delete room: ${task.exception?.message}")
                    }
                }
        } else {
            Log.e("MyRoomFragment", "Room ID is null, cannot delete.")
        }
    }
}
