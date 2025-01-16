package com.example.kotlinactivities.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Room(
    val id: String? = null,
    val imageUrl: String = "",
    val imageUrls: List<String>? = null,
    val title: String = "",
    val people: String = "",
    val price: String = "",
    val rating: String = "",
    val category: String = "", // Add category field
    val bookingStatus: String = "",
    var isFavorited: Boolean = false
) : Parcelable
