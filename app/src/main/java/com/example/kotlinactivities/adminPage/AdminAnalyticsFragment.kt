package com.example.kotlinactivities.adminPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kotlinactivities.databinding.FragmentAdminAnalyticsBinding
import com.google.firebase.database.*

class AdminAnalyticsFragment : Fragment() {

    private var _binding: FragmentAdminAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance()
    private val bookingsRef = database.getReference("bookings")  // Firebase Reference

    // SwipeRefreshLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAnalyticsBinding.inflate(inflater, container, false)

        swipeRefreshLayout = binding.swipeRefreshLayout  // Access the SwipeRefreshLayout

        // Set up the SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener {
            fetchBookingsData()  // Refresh data
        }

        // Initially fetch data
        fetchBookingsData()

        return binding.root
    }

    private fun fetchBookingsData() {
        bookingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var canceledCount = 0
                var upcomingCount = 0
                var rescheduleCount = 0
                var successCount = 0
                var activeRoomsCount = 0
                val currentTime = System.currentTimeMillis()

                for (bookingSnapshot in snapshot.children) {
                    val paymentStatus = bookingSnapshot.child("paymentStatus").value.toString()
                    val startDate = bookingSnapshot.child("startDate").getValue(Long::class.java) ?: 0L
                    val endDate = bookingSnapshot.child("endDate").getValue(Long::class.java) ?: 0L

                    when (paymentStatus) {
                        "Cancelled" -> canceledCount++
                        "Pending Approval" -> {
                            // Check if it's an upcoming booking (startDate is in the future)
                            if (startDate > currentTime) {
                                upcomingCount++
                            }
                        }
                        "Success" -> {
                            successCount++
                            // Check if it's an active booking (currently within the stay period)
                            if (startDate <= currentTime && endDate >= currentTime) {
                                activeRoomsCount++
                            }
                        }
                    }

                    // Upcoming Booking: If startDate is in the future (Pending or Success)
                    if ((paymentStatus == "Pending Approval" || paymentStatus == "Success") && startDate > currentTime) {
                        upcomingCount++
                    }
                }

                // Update UI
                binding.tvCanceledBookings.text = canceledCount.toString()
                binding.tvUpcomingBookings.text = upcomingCount.toString()
                binding.tvRescheduledBookings.text = rescheduleCount.toString()  // Update this if you have a condition for reschedules
                binding.tvActiveRooms.text = activeRoomsCount.toString()

                // Stop the refresh animation
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false  // Stop refreshing if there's an error
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
