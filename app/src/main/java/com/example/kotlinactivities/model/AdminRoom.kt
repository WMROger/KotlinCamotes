package com.example.kotlinactivities.model

data class AdminRoom(
    val name: String,
    val rating: Double,
    val maxPerson: Int,
    val price: String,
    val imageUrl: String, // Remote image URL
    val roomId: String // Ensure roomId is part of the data model
)
