package com.example.kotlinactivities.model

data class AdminBooking(
    val userId: String? = null,
    val userName: String? = null,
    val roomTitle: String? = null,
    val totalPrice: Int? = null, // Updated from String to Int
    val paymentStatus: String? = null,
    val startDate: Long? = null, // Long for timestamps
    val startDateReadable: String? = null,
    val endDate: Long? = null, // Long for timestamps
    val endDateReadable: String? = null,
    val imageUrl: String? = null
)
