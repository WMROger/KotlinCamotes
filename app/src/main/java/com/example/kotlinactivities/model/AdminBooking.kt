package com.example.kotlinactivities.model

data class AdminBooking(
    val id: String? = null,
    val userId: String? = null,
    val userEmail: String? = null,
    val roomTitle: String? = null,
    val roomPrice: Int? = null,
    val totalDays: Int? = null,
    val totalPrice: Int? = null,
    val guestCount: Int? = null,
    val imageUr1: String? = null,
    val paymentMethod: String? = null,
    val paymentStatus: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val startDateReadable: String? = null,
    val endDateReadable: String? = null
)
