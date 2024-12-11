package com.example.hotelbooking

data class Room(
    val id: Int = 0,
    val name: String = "",
    val maxoccupants: Int = 0,
    val price: Int = 0,
    val bookings: String = "", // We can leave this as a String, or you can modify if it's used
    val imageURL: String = "" // Add imageUrl property
)
