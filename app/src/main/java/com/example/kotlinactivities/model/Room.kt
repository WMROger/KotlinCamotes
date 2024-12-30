package com.example.kotlinactivities.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Room(
    val id: String? = null, // Room ID from Firebase
    val imageUrl: String = "",
    val imageUrls: List<String>? = null, // Add this for multiple images
    val title: String = "",
    val people: String = "",
    val price: String = "",
    val rating: String = "",
    val bookingStatus: String = "", // Add booking status
    var isFavorited: Boolean = false
) : Parcelable
