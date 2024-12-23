package com.example.kotlinactivities.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Room(
    val imageUrl: Int, // Drawable resource ID
    val title: String,
    val people: String,
    val price: String,
    val rating: String,
    var isFavorited: Boolean = false
) : Parcelable
