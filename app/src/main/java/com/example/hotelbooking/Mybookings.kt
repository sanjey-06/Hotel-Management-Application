package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView

class MyBookings : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var roomBookingAdapter: RoomBookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mybookings)

        // Retrieve the dynamic data passed via Intent
        val roomId = intent.getIntExtra("ROOM_ID", 0)  // Get Room ID
        val roomType = intent.getStringExtra("ROOM_TYPE") ?: "Unknown"  // Get Room Type
        val startDate = intent.getStringExtra("START_DATE") ?: "Unknown"  // Get Start Date
        val endDate = intent.getStringExtra("END_DATE") ?: "Unknown"  // Get End Date
        val roomImageURL = intent.getStringExtra("ROOM_IMAGE_URL") ?: ""  // Get Room Image URL
        val numberofoccupants = intent.getIntExtra("numberofoccupants", 1)  // Get Number of Occupants
        val numberOfRooms = intent.getIntExtra("NUMBER_OF_ROOMS", 1)  // Get Number of Rooms

        // Log the received data for debugging
        Log.d("MyBookings", "Received data - Room ID: $roomId")
        Log.d("MyBookings", "Room Type: $roomType")
        Log.d("MyBookings", "Start Date: $startDate")
        Log.d("MyBookings", "End Date: $endDate")
        Log.d("MyBookings", "Room Image URL: $roomImageURL")
        Log.d("MyBookings", "Number of Occupants: $numberofoccupants")
        Log.d("MyBookings", "Number of Rooms: $numberOfRooms")

        // Set up RecyclerView with the dynamic data
        recyclerView = findViewById(R.id.rooms_recycler_view)
        Log.d("MyBookings6", "Number of Occupants in Room Object: ${numberofoccupants}")


        // Create a list of Room objects based on the passed data
        val rooms = listOf(
            Room(
                id = roomId,              // Room ID
                name = roomType,          // Room Type
                maxoccupants = 0,         // Set maxoccupants to a placeholder (if unused)
                price = 0,                // Dummy value for price (0 as a placeholder)
                bookings = "",            // Empty string for bookings (if you don't need this)
                imageURL = roomImageURL,  // Room Image URL
                numberofoccupants = numberofoccupants, // Explicitly set numberofoccupants
                numberofrooms = 1         // Default value
            )
        )

// Log the Room object to confirm correct values
        Log.d("MyBookings_Debug", "Room object created: $rooms")


        // Log the data being passed into the adapter
        Log.d("MyBookings4", "Room List Data Passed to Adapter: $rooms")

        // Initialize the RoomBookingAdapter with the rooms data and dates
        roomBookingAdapter = RoomBookingAdapter(
            rooms = rooms,
            startDate = startDate,  // Pass the entire date string
            endDate = endDate     // End date is now the same as start date for your use case
        )
        Log.d("MyBookings5", "Rooms List: $rooms")

        // Log the start and end date for debugging
        //Log.d("MyBookings", "Start Date: $startDate, End Date: $endDate")

        // Set up the RecyclerView with the adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = roomBookingAdapter

        // Bottom Navigation setup
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomnavbar)

        // Set selected item (highlight "Bookings")
        bottomNavigationView.selectedItemId = R.id.page_2

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_1 -> {
                    val intent = Intent(this, Homepage::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.page_2 -> true
                else -> false
            }
        }
    }
}
