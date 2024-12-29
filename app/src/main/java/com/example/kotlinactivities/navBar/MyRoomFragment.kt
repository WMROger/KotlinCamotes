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
            onDeleteClicked = { room -> handleDeleteClick(room) },
            onItemClicked = { room, bookingStatus -> navigateToRoomDetails(room, bookingStatus) }
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
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        myRoomsList.clear()
                        for (roomSnapshot in snapshot.children) {
                            val roomId = roomSnapshot.key
                            val roomTitle = roomSnapshot.child("roomTitle").value as? String ?: "No Title"

                            // Correctly retrieve totalPrice as either a String or Long
                            val totalPrice = when (val price = roomSnapshot.child("totalPrice").value) {
                                is String -> price // If it's stored as a String, use it directly
                                is Long -> "₱$price" // If it's a Long, convert it to a String with "₱"
                                else -> "₱0" // Default to ₱0 if the value is null or unexpected
                            }

                            val guestCount = roomSnapshot.child("guestCount").value as? Long ?: 1L
                            val imageUrl = roomSnapshot.child("imageUrl").value as? String
                                ?: "https://waveaway.scarlet2.io/assets/ic_placeholder.png"
                            val rating = roomSnapshot.child("rating").value as? String ?: "4.9"

                            // Retrieve both paymentStatus and status fields
                            val paymentStatus = roomSnapshot.child("paymentStatus").value as? String ?: ""
                            val status = roomSnapshot.child("status").value as? String ?: "Pending"

                            // Log debugging information
                            Log.d("MyRoomFragment", "Room ID: $roomId, paymentStatus: $paymentStatus, status: $status")

                            // Determine the final booking status dynamically
                            val bookingStatus = when {
                                paymentStatus.equals("Success", ignoreCase = true) -> "Approved"
                                status.equals("Pending Approval", ignoreCase = true) || status.equals("Pending", ignoreCase = true) -> "Pending Approval"
                                status.equals("Rejected", ignoreCase = true) -> "Rejected"
                                else -> "Unknown Status"
                            }

                            myRoomsList.add(
                                Room(
                                    id = roomId,
                                    imageUrl = imageUrl,
                                    title = roomTitle,
                                    people = "$guestCount People",
                                    price = totalPrice, // Use the correctly retrieved totalPrice here
                                    rating = rating,
                                    bookingStatus = bookingStatus,
                                    isFavorited = false
                                )
                            )
                        }
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
        room.isFavorited = !room.isFavorited
        myRoomsAdapter.notifyDataSetChanged()
        Log.d("MyRoomFragment", "Favorite clicked for room: ${room.title}, state: ${room.isFavorited}")
    }

    private fun handleDeleteClick(room: Room) {
        val roomId = room.id
        if (roomId != null) {
            databaseReference.child(roomId).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("MyRoomFragment", "Room deleted successfully: $roomId")
                        myRoomsAdapter.removeRoom(room)
                    } else {
                        Log.e("MyRoomFragment", "Failed to delete room: ${task.exception?.message}")
                    }
                }
        } else {
            Log.e("MyRoomFragment", "Room ID is null, cannot delete.")
        }
    }
}
