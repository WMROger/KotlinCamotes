package com.example.kotlinactivities.model

data class Room(
    val imageUrl: Int, // Use Int for resource IDs
    val title: String,
    val people: String,
    val price: String,
    val rating: String
)
