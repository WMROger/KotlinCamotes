package com.example.kotlinactivities.userPage.navBar

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
import com.example.kotlinactivities.userPage.homePage.RoomDetailsActivity
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
        loadRoomsFromFirebase(view)

        return view
    }

    private fun loadRoomsFromFirebase(view: View) {
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId != null) {
            databaseReference.orderByChild("userId").equalTo(currentUserId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        myRoomsList.clear()
                        for (roomSnapshot in snapshot.children) {
                            val roomId = roomSnapshot.key
                            val roomTitle = roomSnapshot.child("roomTitle").value as? String ?: "No Title"

                            // Handle price properly
                            val totalPrice = when (val price = roomSnapshot.child("totalPrice").value) {
                                is String -> price
                                is Long -> "₱$price"
                                else -> "₱0"
                            }

                            val guestCount = roomSnapshot.child("guestCount").value as? Long ?: 1L
                            val imageUrl = roomSnapshot.child("imageUrl").value as? String
                                ?: "https://waveaway.scarlet2.io/assets/ic_placeholder.png"
                            val rating = roomSnapshot.child("rating").value as? String ?: "4.9"

                            val paymentStatus = roomSnapshot.child("paymentStatus").value as? String ?: ""
                            val status = roomSnapshot.child("status").value as? String ?: "Pending"

                            val bookingStatus = when {
                                paymentStatus.equals("Cancelled", ignoreCase = true) -> "Cancelled"
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
                                    price = totalPrice,
                                    rating = rating,
                                    bookingStatus = bookingStatus,
                                    isFavorited = false
                                )
                            )
                        }
                        myRoomsAdapter.notifyDataSetChanged()

                        // ✅ Toggle UI based on data availability
                        if (myRoomsList.isNotEmpty()) {
                            myRoomsRecyclerView.visibility = View.VISIBLE
                            view.findViewById<View>(R.id.emptyStateImage)?.visibility = View.GONE
                            view.findViewById<View>(R.id.emptyStateTitle)?.visibility = View.GONE
                            view.findViewById<View>(R.id.emptyStateSubtitle)?.visibility = View.GONE
                            view.findViewById<View>(R.id.homePageLink)?.visibility = View.GONE
                        } else {
                            myRoomsRecyclerView.visibility = View.GONE
                            view.findViewById<View>(R.id.emptyStateImage)?.visibility = View.VISIBLE
                            view.findViewById<View>(R.id.emptyStateTitle)?.visibility = View.VISIBLE
                            view.findViewById<View>(R.id.emptyStateSubtitle)?.visibility = View.VISIBLE
                            view.findViewById<View>(R.id.homePageLink)?.visibility = View.VISIBLE
                        }
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
