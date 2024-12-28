package com.example.kotlinactivities.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Room(
    val id: String? = null, // Room ID from Firebase
    val imageUrl: String = "",
    val title: String = "",
    val people: String = "",
    val price: String = "",
    val rating: String = "",
    val isFavorited: Boolean = false
): Parcelable
