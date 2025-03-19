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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.NonCancellable.isCancelled
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat


private const val NOTIFICATION_ID = 1
private const val CHANNEL_ID = "booking_notifications"
private const val PERMISSION_REQUEST_CODE = 1001
private const val REQUEST_CODE_STORAGE = 1002

class ApprovalFragment : Fragment() {

    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingsAdapter: AdminBookingAdapter
    private val bookingsList: MutableList<AdminBooking> = mutableListOf()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var tabLayout: TabLayout
    private val userCache = mutableMapOf<String, String>()

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
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingsList.clear()
                val today = System.currentTimeMillis()

                for (bookingSnapshot in snapshot.children) {
                    try {
                        if (!bookingSnapshot.hasChildren()) continue

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
                            endDateReadable = bookingSnapshot.child("endDateReadable").getValue(String::class.java),
                            userName = "" // Will be fetched later
                        )

                        if (filterCondition(booking)) {
                            bookingsList.add(booking)

                            // Fetch username asynchronously and update UI
                            booking.userId?.let { userId ->
                                fetchUserName(userId) { userName ->
                                    booking.userName = userName
                                    bookingsAdapter.notifyItemChanged(bookingsList.indexOf(booking))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ApprovalFragment", "Error parsing booking: ${e.message}")
                    }
                }

                // Sort bookings by closest start date
                bookingsList.sortBy { kotlin.math.abs(it.startDate!! - today) }

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

    private fun fetchUserName(userId: String, callback: (String) -> Unit) {
        Log.d("fetchUserName", "Fetching username for userId: $userId")
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").getValue(String::class.java) ?: "Unknown User"
                Log.d("fetchUserName", "Username found: $userName")
                callback(userName)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchUserName", "Error fetching username", error.toException())
                callback("Unknown User")
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
                        "Dear user, your booking has been cancelled."
                    } else {
                        "Dear user, your booking has been approved!"
                    }

                    // Send email notification
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            sendEmail(userEmail, subject, message)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // Send push notification
                    sendNotification(subject, message)

                    val toastMessage = if (isCancelled) "Booking cancelled and notification sent!" else "Payment confirmed and notification sent!"
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()

                    // Reload bookings
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

    private fun isToday(booking: AdminBooking): Boolean {
        if (booking.startDate == null || booking.endDate == null || booking.paymentStatus.isNullOrEmpty()) {
            return false
        }

        val todayMillis = System.currentTimeMillis()

        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = booking.startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = startCalendar.timeInMillis

        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = booking.endDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endMillis = endCalendar.timeInMillis

        Log.d(
            "BookingCheck",
            "Booking ID: ${booking.id}, StartDate: ${booking.startDateReadable}, EndDate: ${booking.endDateReadable}, Today: ${
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(todayMillis)
            }, PaymentStatus: ${booking.paymentStatus}"
        )

        return booking.paymentStatus.equals("Success", ignoreCase = true) &&
                todayMillis in startMillis..endMillis
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

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(context, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(context, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE)
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Booking Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for booking status updates"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Check permission for Android 13+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)
                return
            }
        }

        // Build the notification
        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Replace with your actual notification icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(requireContext()).notify(NOTIFICATION_ID, notification)
    }


}
