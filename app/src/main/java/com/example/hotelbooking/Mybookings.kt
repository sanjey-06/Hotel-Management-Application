package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MyBookings : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var roomBookingAdapter: RoomBookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mybookings)

        setupBottomNavigation()
        val userId = getCurrentUserId()
        if (userId != null) {
            fetchBookingDetails(userId)
        } else {
            Log.e("MyBookings", "User not logged in!")
            showErrorAndExit()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomnavbar)
        bottomNavigationView.selectedItemId = R.id.page_2 // Highlight "My Bookings"
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_1 -> {
                    navigateToHomepage()
                    true
                }
                R.id.page_2 -> true // Stay on the current page
                else -> false
            }
        }
    }

    private fun navigateToHomepage() {
        val intent = Intent(this, Homepage::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun fetchBookingDetails(userId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("bookings")

        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val bookingsWithDates = mutableListOf<Pair<List<Room>, Pair<String, String>>>()

                for (bookingSnapshot in snapshot.children) {
                    val bookingUserId = bookingSnapshot.child("userId").getValue(String::class.java)

                    if (bookingUserId == userId) {
                        // Fetch booking-level data
                        val startDate = bookingSnapshot.child("startDate").getValue(String::class.java) ?: "Unknown"
                        val endDate = bookingSnapshot.child("endDate").getValue(String::class.java) ?: "Unknown"

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

                            // Now fetch numberOfOccupants from the booking node
                            val numberOfOccupants = bookingSnapshot.child("numberOfOccupants").getValue(Int::class.java) ?: 0

                            // Log each field to check data retrieval
                            Log.d("MyBookings", "Room Details:")
                            Log.d("MyBookings", "roomId: $roomId")
                            Log.d("MyBookings", "roomName: $roomName")
                            Log.d("MyBookings", "roomImageURL: $roomImageURL")
                            Log.d("MyBookings", "maxOccupants: $maxOccupants")
                            Log.d("MyBookings", "price: $price")
                            Log.d("MyBookings", "numberOfRooms: $numberOfRooms")
                            Log.d("MyBookings", "numberOfOccupants: $numberOfOccupants")

                            val room = Room(
                                id = roomId,
                                name = roomName,
                                maxoccupants = maxOccupants,
                                price = price,
                                bookings = "",
                                imageURL = roomImageURL,
                                numberofoccupants = numberOfOccupants,
                                numberofrooms = numberOfRooms
                            )
                            rooms.add(room)
                        }

                        // Store the rooms and their corresponding dates as a pair
                        bookingsWithDates.add(Pair(rooms, Pair(startDate, endDate)))
                    }
                }

                if (bookingsWithDates.isNotEmpty()) {
                    setupRecyclerView(bookingsWithDates)
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


    private fun setupRecyclerView(bookingsWithDates: List<Pair<List<Room>, Pair<String, String>>>) {
        Log.d("MyBookings", "Setting up RecyclerView with ${bookingsWithDates.size} bookings")

        recyclerView = findViewById(R.id.rooms_recycler_view)
        roomBookingAdapter = RoomBookingAdapter(bookingsWithDates)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = roomBookingAdapter
    }


    private fun showErrorAndExit() {
        Toast.makeText(this, "No bookings found or error fetching data. Returning to homepage.", Toast.LENGTH_LONG).show()
        navigateToHomepage()
    }
}
