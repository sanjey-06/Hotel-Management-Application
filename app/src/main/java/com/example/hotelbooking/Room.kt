package com.example.hotelbooking

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Room(
    val id: Int,
    val name: String,
    val maxoccupants: Int,
    val price: Int,
    val bookings: String,
    val imageURL: String,
    var numberofoccupants: Int = 0,
    var numberofrooms: Int = 1 // Default value
) : Parcelable

