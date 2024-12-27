package com.example.kotlinactivities.navBar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.RoomAdapter
import com.example.kotlinactivities.model.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyRoomFragment : Fragment() {

    private lateinit var myRoomsRecyclerView: RecyclerView
    private lateinit var myRoomsAdapter: RoomAdapter

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
        myRoomsAdapter = RoomAdapter(myRoomsList) { room ->
            room.isFavorited = !room.isFavorited
            myRoomsAdapter.notifyDataSetChanged()
        }
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
                            val roomTitle = roomSnapshot.child("roomTitle").value as? String ?: "No Title"
                            val totalDays = roomSnapshot.child("totalDays").value as? Long ?: 1L
                            val roomPrice = roomSnapshot.child("roomPrice").value as? String ?: "₱0/night"
                            val totalPrice = roomSnapshot.child("totalPrice").value as? String ?: "₱0"
                            val guestCount = roomSnapshot.child("guestCount").value as? Long ?: 1L
                            val rating = "4.9" // Replace with dynamic rating if available

                            // Add the room to the list
                            myRoomsList.add(
                                Room(
                                    imageUrl = R.drawable.ic_cupids_deluxe,
                                    title = roomTitle,
                                    people = "$guestCount People",
                                    price = totalPrice,
                                    rating = rating,
                                    isFavorited = false
                                )
                            )
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
}
