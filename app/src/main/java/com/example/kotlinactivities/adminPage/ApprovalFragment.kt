package com.example.kotlinactivities.admin

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
import com.example.kotlinactivities.adapter.BookingAdapter
import com.example.kotlinactivities.model.Booking
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*

class ApprovalFragment : Fragment() {

    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingsAdapter: BookingAdapter
    private val bookingsList: MutableList<Booking> = mutableListOf()
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

        bookingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        bookingsAdapter = BookingAdapter(bookingsList)
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

    private fun loadBookings(filterCondition: (Booking) -> Boolean) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingsList.clear()

                for (bookingSnapshot in snapshot.children) {
                    try {
                        // Map data to Booking model
                        val booking = bookingSnapshot.getValue(Booking::class.java)
                        if (booking != null && filterCondition(booking)) {
                            bookingsList.add(booking)
                            Log.d("ApprovalFragment", "Booking loaded: $booking")
                        }
                    } catch (e: Exception) {
                        // Log the error and skip the problematic data
                        Log.e("FirebaseError", "Error parsing booking: ${e.message}")
                    }
                }

                // Notify the adapter of data changes
                bookingsAdapter.notifyDataSetChanged()
                Log.d("ApprovalFragment", "Total bookings: ${bookingsList.size}")
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

    private fun isToday(booking: Booking): Boolean {
        return try {
            val today = System.currentTimeMillis()
            val startOfDay = today - (today % (24 * 60 * 60 * 1000)) // Start of the day
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000) // End of the day
            booking.startDate in startOfDay..endOfDay
        } catch (e: Exception) {
            false
        }
    }

    private fun isUpcoming(booking: Booking): Boolean {
        return try {
            booking.startDate > System.currentTimeMillis() && booking.paymentStatus != "Accepted"
        } catch (e: Exception) {
            false
        }
    }

    private fun isRescheduled(booking: Booking): Boolean {
        return booking.paymentStatus == "Rescheduled"
    }
}
