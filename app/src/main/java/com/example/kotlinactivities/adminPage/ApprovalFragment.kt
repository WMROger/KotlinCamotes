package com.example.kotlinactivities.adminPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminPage.adminAdapter.AdminBookingAdapter
import com.example.kotlinactivities.model.AdminBooking
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*

class ApprovalFragment : Fragment() {

    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingsAdapter: AdminBookingAdapter
    private val bookingsList: MutableList<AdminBooking> = mutableListOf()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_approval, container, false)

        // Initialize views
        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView)
        tabLayout = view.findViewById(R.id.tabLayout)

        // Initialize RecyclerView
        bookingsAdapter = AdminBookingAdapter(bookingsList, ::fetchUserName)
        bookingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        bookingsRecyclerView.adapter = bookingsAdapter

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("bookings")

        // Add tabs to TabLayout
        setupTabLayout()

        // Load today's bookings by default
        loadBookings { isToday(it) }

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadBookings { isToday(it) } // Load Today's Bookings
                    1 -> loadBookings { isUpcoming(it) } // Load Upcoming Bookings
                    2 -> loadBookings { isRescheduled(it) } // Load Rescheduled Bookings
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return view
    }

    private fun setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Today's bookings"))
        tabLayout.addTab(tabLayout.newTab().setText("Upcoming bookings"))
        tabLayout.addTab(tabLayout.newTab().setText("Rescheduled"))
    }

    private fun loadBookings(filterCondition: (AdminBooking) -> Boolean) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingsList.clear()

                for (bookingSnapshot in snapshot.children) {
                    try {
                        Log.d("ApprovalFragment", "Raw data: ${bookingSnapshot.value}")
                        val booking = bookingSnapshot.getValue(AdminBooking::class.java)
                        if (booking != null) {
                            if (filterCondition(booking)) {
                                bookingsList.add(booking)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ApprovalFragment", "Error parsing booking: ${e.message}")
                    }
                }

                bookingsAdapter.notifyDataSetChanged()
                Log.d("ApprovalFragment", "Total bookings loaded: ${bookingsList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Failed to load bookings: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun fetchUserName(userId: String, callback: (String) -> Unit) {
        val usersReference = FirebaseDatabase.getInstance().getReference("Users")
        usersReference.child(userId).get().addOnSuccessListener { snapshot ->
            val fullName = snapshot.child("name").getValue(String::class.java)
            if (fullName != null && fullName.isNotEmpty()) {
                callback(fullName)
                Log.d("fetchUserName", "Fetched name for userId $userId: $fullName")
            } else {
                callback("Unknown")
                Log.e("fetchUserName", "No name found for userId: $userId")
            }
        }.addOnFailureListener { exception ->
            callback("Unknown")
            Log.e("fetchUserName", "Error fetching name for userId: $userId", exception)
        }
    }

    private fun isToday(booking: AdminBooking): Boolean {
        val today = System.currentTimeMillis()
        val startOfDay = today - (today % (24 * 60 * 60 * 1000)) // Start of the day in milliseconds
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1    // End of the day in milliseconds

        // Check if startDate is not null and falls within today's range
        val startDate = booking.startDate ?: return false

        return startDate in startOfDay..endOfDay &&
                booking.paymentStatus.equals("Success", ignoreCase = true)
    }

    private fun isUpcoming(booking: AdminBooking): Boolean {
        val result = booking.paymentStatus == "Pending Approval"
        Log.d("FilterCheck", "isUpcoming: Booking ${booking.userId} -> $result (paymentStatus: ${booking.paymentStatus})")
        return result
    }

    private fun isRescheduled(booking: AdminBooking): Boolean {
        val result = booking.paymentStatus == "Rescheduled"
        Log.d("FilterCheck", "isRescheduled: Booking ${booking.userId} -> $result")
        return result
    }
}
