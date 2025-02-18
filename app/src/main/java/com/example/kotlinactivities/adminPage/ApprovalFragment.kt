package com.example.kotlinactivities.adminPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adminPage.adminAdapter.AdminBookingAdapter
import com.example.kotlinactivities.model.AdminBooking
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        val filterIcon = view.findViewById<ImageView>(R.id.filterIcon)
        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView)
        tabLayout = view.findViewById(R.id.tabLayout)

        // Set up RecyclerView
        bookingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Add spacing of 16dp between items
        bookingsRecyclerView.addItemDecoration(SpacingItemDecoration(16))

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("bookings")

        // Set up TabLayout
        setupTabLayout()

        // Load today's bookings by default
        loadBookings { isToday(it) }

        // Handle TabLayout selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadBookings { isToday(it) }
                    1 -> loadBookings { isUpcoming(it) }
                    2 -> loadBookings { isRescheduled(it) }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Handle filter icon click
        filterIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Filter clicked!", Toast.LENGTH_SHORT).show()
            // Implement your filter functionality here
        }

        return view
    }


    private fun setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Today's bookings"))
        tabLayout.addTab(tabLayout.newTab().setText("Upcoming bookings"))
        tabLayout.addTab(tabLayout.newTab().setText("Rescheduled"))
        tabLayout.addTab(tabLayout.newTab().setText("Canceled"))
        tabLayout.addTab(tabLayout.newTab().setText("Extend Stay"))
    }

    private fun loadBookings(filterCondition: (AdminBooking) -> Boolean) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingsList.clear()

                for (bookingSnapshot in snapshot.children) {
                    try {
                        if (!bookingSnapshot.hasChildren()) {
                            Log.e("FirebaseError", "Invalid booking data: ${bookingSnapshot.value}")
                            continue
                        }

                        val booking = AdminBooking(
                            id = bookingSnapshot.key,
                            userId = bookingSnapshot.child("userId").getValue(String::class.java),
                            userEmail = bookingSnapshot.child("userEmail").getValue(String::class.java),
                            roomTitle = bookingSnapshot.child("roomTitle").getValue(String::class.java),
                            roomPrice = bookingSnapshot.child("roomPrice").getValue(Int::class.java),
                            totalDays = bookingSnapshot.child("totalDays").getValue(Int::class.java),
                            totalPrice = bookingSnapshot.child("totalPrice").getValue(Int::class.java),
                            guestCount = bookingSnapshot.child("guestCount").getValue(Int::class.java),
                            imageUr1 = bookingSnapshot.child("imageUr1").getValue(String::class.java),
                            paymentMethod = bookingSnapshot.child("paymentMethod").getValue(String::class.java),
                            paymentStatus = bookingSnapshot.child("paymentStatus").getValue(String::class.java),
                            startDate = bookingSnapshot.child("startDate").getValue(Long::class.java),
                            endDate = bookingSnapshot.child("endDate").getValue(Long::class.java),
                            startDateReadable = bookingSnapshot.child("startDateReadable").getValue(String::class.java),
                            endDateReadable = bookingSnapshot.child("endDateReadable").getValue(String::class.java)
                        )

                        if (filterCondition(booking)) {
                            bookingsList.add(booking)
                        }

                    } catch (e: Exception) {
                        Log.e("ApprovalFragment", "Error parsing booking: ${e.message}")
                    }
                }

                Log.d("FinalListSize", "Total Bookings Displayed: ${bookingsList.size}")

                bookingsAdapter = AdminBookingAdapter(
                    bookingsList,
                    ::fetchUserName,
                    isUpcomingTab = (tabLayout.selectedTabPosition == 1),
                    onPaidClick = { bookingId -> updatePaymentStatus(bookingId) }
                )
                bookingsRecyclerView.adapter = bookingsAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load bookings: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun updatePaymentStatus(bookingId: String) {
        val bookingRef = databaseReference.child(bookingId)

        bookingRef.child("paymentStatus").setValue("Success")
            .addOnSuccessListener {
                bookingRef.child("paymentMethod").get()
                    .addOnSuccessListener { snapshot ->
                        val method = snapshot.getValue(String::class.java) ?: "Unknown"
                        val message = if (method.equals("Cash", ignoreCase = true)) "Payment confirmed via Cash!" else "Payment confirmed!"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        // Reload "Today's Bookings" to reflect updated paid bookings
                        loadBookings { isToday(it) }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
            }
    }




    private fun reloadAllTabs() {
        tabLayout.getTabAt(0)?.let { loadBookings { isToday(it) } }  // Today's Bookings
        tabLayout.getTabAt(1)?.let { loadBookings { isUpcoming(it) } }  // Upcoming Bookings
    }





    private fun fetchUserName(userId: String, callback: (String) -> Unit) {
        val usersReference = FirebaseDatabase.getInstance().getReference("Users")
        usersReference.child(userId).get().addOnSuccessListener { snapshot ->
            val fullName = snapshot.child("name").getValue(String::class.java)
            callback(fullName ?: "Unknown")
        }.addOnFailureListener {
            callback("Unknown")
        }
    }

    private fun isToday(booking: AdminBooking): Boolean {
        if (booking.startDateReadable.isNullOrEmpty() || booking.paymentStatus.isNullOrEmpty()) {
            return false
        }

        // Format of stored date in Firebase
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        // Get today's date
        val todayDate = dateFormat.format(Date())

        Log.d("BookingCheck", "Booking ID: ${booking.id}, StartDateReadable: ${booking.startDateReadable}, Today: $todayDate, PaymentStatus: ${booking.paymentStatus}, PaymentMethod: ${booking.paymentMethod}")

        // Allow "Success" payments to appear in "Today's Bookings" even if startDate is later
        return booking.paymentStatus.equals("Success", ignoreCase = true) &&
                (booking.startDateReadable == todayDate || booking.startDateReadable > todayDate)
    }







    private fun isUpcoming(booking: AdminBooking): Boolean {
        return booking.paymentStatus == "Pending Approval"
    }

    private fun isRescheduled(booking: AdminBooking): Boolean {
        return booking.paymentStatus == "Rescheduled"
    }
}
