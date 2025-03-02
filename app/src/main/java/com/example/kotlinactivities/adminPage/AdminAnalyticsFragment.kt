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
import java.util.*

class AdminAnalyticsFragment : Fragment() {

    private var _binding: FragmentAdminAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance()
    private val bookingsRef = database.getReference("bookings")  // Firebase Reference

    // SwipeRefreshLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        _binding = FragmentAdminAnalyticsBinding.inflate(inflater, container, false)
        return binding.root // Return the root view of the binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = binding.swipeRefreshLayout  // Access the SwipeRefreshLayout

        // Set up the SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener {
            fetchBookingsData()  // Refresh data
        }

        // Initially fetch data
        fetchBookingsData()
    }

    private fun fetchBookingsData() {
        bookingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var canceledCount = 0
                var upcomingCount = 0
                var rescheduleCount = 0
                var successCount = 0
                var activeRoomsCount = 0
                var totalRevenue = 0.0  // Total revenue calculation
                var previousMonthRevenue = 0.0  // Previous month's revenue
                val currentTime = System.currentTimeMillis()

                // Get the current and previous month
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH) // Month starts from 0 (Jan = 0)

                // Get previous month and handle year change
                calendar.add(Calendar.MONTH, -1)
                val previousMonth = calendar.get(Calendar.MONTH)
                val previousYear = calendar.get(Calendar.YEAR)

                for (bookingSnapshot in snapshot.children) {
                    val paymentStatus = bookingSnapshot.child("paymentStatus").value.toString()
                    val startDate = bookingSnapshot.child("startDate").getValue(Long::class.java) ?: 0L
                    val endDate = bookingSnapshot.child("endDate").getValue(Long::class.java) ?: 0L
                    val totalPrice = bookingSnapshot.child("totalPrice").getValue(Double::class.java) ?: 0.0

                    // Convert startDate to calendar to check the month
                    val bookingCalendar = Calendar.getInstance()
                    bookingCalendar.timeInMillis = startDate
                    val bookingMonth = bookingCalendar.get(Calendar.MONTH)
                    val bookingYear = bookingCalendar.get(Calendar.YEAR)

                    // Check if the booking is in the current month and add to total revenue
                    if (bookingMonth == currentMonth && bookingYear == currentYear && paymentStatus == "Success") {
                        totalRevenue += totalPrice
                    }

                    // Check if the booking is in the previous month and add to previous month revenue
                    if (bookingMonth == previousMonth && bookingYear == previousYear && paymentStatus == "Success") {
                        previousMonthRevenue += totalPrice
                    }

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

                // Calculate revenue percentage change
                val revenuePercentageChange = if (previousMonthRevenue > 0) {
                    ((totalRevenue - previousMonthRevenue) / previousMonthRevenue) * 100
                } else {
                    0.0
                }

                // Update UI
                binding.tvCanceledBookings.text = canceledCount.toString()
                binding.tvUpcomingBookings.text = upcomingCount.toString()
                binding.tvRescheduledBookings.text = rescheduleCount.toString()
                binding.tvActiveRooms.text = activeRoomsCount.toString()
                binding.tvRevenue.text = "₱%.2f".format(totalRevenue) // Format as currency
                binding.tvRevenuePercentage.text = "%.2f%%".format(revenuePercentageChange) // Display percentage

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
        _binding = null  // Set to null to avoid memory leaks
    }
}
