package com.example.kotlinactivities.model

data class AdminBooking(
    val userId: String? = null,
    val userName: String? = null,
    val roomTitle: String? = null,
    val totalPrice: String? = null,
    val paymentStatus: String? = null,
    val startDate: Long? = null,
    val startDateReadable: String? = null,
    val endDateReadable: String? = null,
    val imageUrl: String? = null
)
