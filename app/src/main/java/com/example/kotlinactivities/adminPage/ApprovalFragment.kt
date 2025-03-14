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
import com.example.kotlinactivities.network.sendEmail
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import kotlinx.coroutines.NonCancellable.isCancelled
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
                    0 -> loadBookings { isToday(it) }        // Today's Bookings
                    1 -> loadBookings { isUpcoming(it) }     // Upcoming Bookings
                    2 -> loadBookings { isRescheduled(it) }  // Rescheduled Bookings
                    3 -> loadBookings { isCancelled(it) }    // Canceled Bookings
//                    4 -> loadBookings { isExtendedStay(it) } // Extend Stay Bookings
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
                        Log.d("Debug", "Booking StartDate: ${booking.startDateReadable}, Expected Format: MMM dd, yyyy")

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

    private fun updatePaymentStatus(bookingId: String, isCancelled: Boolean = false) {
        val bookingRef = databaseReference.child(bookingId)
        val newStatus = if (isCancelled) "Cancelled" else "Success"

        bookingRef.child("paymentStatus").setValue(newStatus)
            .addOnSuccessListener {
                bookingRef.get().addOnSuccessListener { snapshot ->
                    val userEmail = snapshot.child("userEmail").getValue(String::class.java) ?: return@addOnSuccessListener
                    val subject = if (isCancelled) "Booking Cancelled" else "Booking Approved"
                    val message = if (isCancelled) {
                        "Dear user,\n\nWe regret to inform you that your booking has been cancelled.\n\nIf you have any concerns, please contact us."
                    } else {
                        "Dear user,\n\nYour booking has been approved!\n\nThank you for choosing us."
                    }

                    // Launch a coroutine to send the email
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            sendEmail(userEmail, subject, message)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    val toastMessage = if (isCancelled) "Booking cancelled and email sent!" else "Payment confirmed and email sent!"
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()

                    // Reload bookings after update
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
        if (booking.startDate == null || booking.endDate == null || booking.paymentStatus.isNullOrEmpty()) {
            return false
        }

        val today = System.currentTimeMillis()

        Log.d(
            "BookingCheck",
            "Booking ID: ${booking.id}, StartDate: ${booking.startDateReadable}, EndDate: ${booking.endDateReadable}, Today: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(today)}, PaymentStatus: ${booking.paymentStatus}"
        )

        return booking.paymentStatus.equals("Success", ignoreCase = true) &&
                today in booking.startDate..booking.endDate
    }


    private fun isUpcoming(booking: AdminBooking): Boolean {
        return booking.paymentStatus == "Pending Approval"
    }

    private fun isRescheduled(booking: AdminBooking): Boolean {
        return booking.paymentStatus == "Rescheduled"
    }

    private fun isCancelled(booking: AdminBooking): Boolean {
        return booking.paymentStatus.equals("Cancelled", ignoreCase = true)
    }

//    private fun isExtendedStay(booking: AdminBooking): Boolean {
//        return booking.paymentStatus.equals("Extended Stay", ignoreCase = true)
//    }

}
