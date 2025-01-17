package com.example.kotlinactivities.model

import java.util.Date
import java.util.Locale
data class Booking(
    val endDate: Long = 0L, // Use Long for timestamps
    val guestCount: Int = 0,
    var userName: String = "", // Added to hold the first name
    val imageUrl: String = "",
    val paymentMethod: String = "",
    val paymentStatus: String = "",
    val roomPrice: Int = 0,
    val roomTitle: String = "",
    val startDate: Long = 0L, // Use Long for timestamps
    val totalDays: Int = 0,
    val totalPrice: Int = 0,
    val userEmail: String = "",
    val userId: String = ""
) {
    // Safely convert startDate to Long
    val startDateAsLong: Long
        get() = (startDate as? Long) ?: (startDate as? String)?.toLongOrNull() ?: 0L

    // Safely convert endDate to Long
    val endDateAsLong: Long
        get() = (endDate as? Long) ?: (endDate as? String)?.toLongOrNull() ?: 0L

    // Safely convert guestCount to Int
    val guestCountAsInt: Int
        get() = (guestCount as? Int) ?: (guestCount as? String)?.toIntOrNull() ?: 0

    // Safely convert roomPrice to Int
    val roomPriceAsInt: Int
        get() = (roomPrice as? Int) ?: (roomPrice as? String)?.toIntOrNull() ?: 0

    // Safely convert totalDays to Int
    val totalDaysAsInt: Int
        get() = (totalDays as? Int) ?: (totalDays as? String)?.toIntOrNull() ?: 0

    // Safely convert totalPrice to Int
    val totalPriceAsInt: Int
        get() = (totalPrice as? Int) ?: (totalPrice as? String)?.toIntOrNull() ?: 0
}
