package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MyBookings : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var roomBookingAdapter: RoomBookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mybookings)

        // Get the current logged-in user's ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("MyBookings", "User not logged in!")
            showErrorAndExit()
            return
        }

        Log.d("MyBookings", "Logged in user ID: $userId")

        // Fetch booking details from the database
        fetchBookingDetails(userId)
    }

    private fun fetchBookingDetails(userId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("bookings")

        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val bookings = mutableListOf<List<Room>>()
                var startDate = "Unknown"
                var endDate = "Unknown"

                for (bookingSnapshot in snapshot.children) {
                    val bookingUserId = bookingSnapshot.child("userId").getValue(String::class.java)

                    if (bookingUserId == userId) {
                        // Fetch booking-level data
                        startDate = bookingSnapshot.child("startDate").getValue(String::class.java) ?: "Unknown"
                        endDate = bookingSnapshot.child("endDate").getValue(String::class.java) ?: "Unknown"

                        // Handle the 'rooms' array inside the booking
                        val rooms = mutableListOf<Room>()
                        val roomsArray = bookingSnapshot.child("rooms")
                        for (roomSnapshot in roomsArray.children) {
                            val roomId = roomSnapshot.child("roomId").getValue(Int::class.java) ?: 0
                            val roomName = roomSnapshot.child("roomName").getValue(String::class.java) ?: "Unknown"
                            val roomImageURL = roomSnapshot.child("roomImageURL").getValue(String::class.java) ?: ""
                            val maxOccupants = roomSnapshot.child("maxOccupants").getValue(Int::class.java) ?: 0
                            val price = roomSnapshot.child("price").getValue(Int::class.java) ?: 0
                            val numberOfRooms = roomSnapshot.child("numberOfRooms").getValue(Int::class.java) ?: 1

                            val room = Room(
                                id = roomId,
                                name = roomName,
                                maxoccupants = maxOccupants,
                                price = price,
                                bookings = "",
                                imageURL = roomImageURL,
                                numberofoccupants = 0, // You can customize this value if needed
                                numberofrooms = numberOfRooms
                            )
                            rooms.add(room)
                        }

                        // Add the rooms of this booking as a group to the bookings list
                        if (rooms.isNotEmpty()) {
                            bookings.add(rooms)
                        }
                    }
                }

                if (bookings.isNotEmpty()) {
                    setupRecyclerView(bookings, startDate, endDate)
                } else {
                    Log.e("MyBookings", "No bookings found for user: $userId")
                    showErrorAndExit()
                }
            } else {
                Log.e("MyBookings", "No bookings found in database.")
                showErrorAndExit()
            }
        }.addOnFailureListener { exception ->
            Log.e("MyBookings", "Failed to fetch booking details: ${exception.message}")
            showErrorAndExit()
        }
    }

    private fun setupRecyclerView(bookings: List<List<Room>>, startDate: String, endDate: String) {
        Log.d("MyBookings", "Setting up RecyclerView with ${bookings.size} bookings")

        recyclerView = findViewById(R.id.rooms_recycler_view)

        // Initialize the RoomBookingAdapter with fetched data and pass startDate and endDate
        roomBookingAdapter = RoomBookingAdapter(bookings, startDate, endDate)

        // Set up the RecyclerView with the adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = roomBookingAdapter
    }

    private fun showErrorAndExit() {
        Toast.makeText(this, "No bookings found or error fetching data. Returning to homepage.", Toast.LENGTH_LONG).show()
        val intent = Intent(this, Homepage::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
