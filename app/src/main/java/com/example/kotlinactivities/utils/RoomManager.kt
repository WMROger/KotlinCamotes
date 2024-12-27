package com.example.kotlinactivities.utils

import com.example.kotlinactivities.model.Room

object RoomManager {
    val bookedRooms = mutableListOf<Room>()

    fun addRoom(room: Room) {
        // Avoid duplicates
        if (bookedRooms.none { it.title == room.title }) {
            bookedRooms.add(room)
        }
    }

    fun getRooms(): List<Room> = bookedRooms
}
