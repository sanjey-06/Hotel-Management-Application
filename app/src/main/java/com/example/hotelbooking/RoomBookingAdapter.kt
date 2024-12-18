package com.example.hotelbooking

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RoomBookingAdapter(
    private val rooms: List<Room>,
    private val startDate: String,
    private val endDate: String
) : RecyclerView.Adapter<RoomBookingAdapter.RoomViewHolder>() {
    init {
        Log.d("RoomBookingAdapter_Debug", "Rooms received: $rooms")
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        // Inflate the layout for each item in the RecyclerView
        val view = LayoutInflater.from(parent.context).inflate(R.layout.roomlist, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]

        // Log the room data being displayed in the RecyclerView
        Log.d(
            "RoomBookingAdapter",
            "Room: ${room.name}, Occupants: ${room.numberofoccupants}, Dates: $startDate to $endDate"
        )

        // Set the room name and the date range
        holder.roomTypeTextView.text = room.name
        holder.dateTextView.text = "$startDate to $endDate"  // Directly display the passed dates
        holder.occupantsTextView.text = "Occupants: ${room.numberofoccupants}"

        // Load the room image using Glide
        Glide.with(holder.itemView.context)
            .load(room.imageURL)
            .into(holder.roomImageView)

    }

    override fun getItemCount(): Int = rooms.size

    // ViewHolder to hold the room details
    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // References to the UI elements in roomlist.xml
        val roomTypeTextView: TextView = itemView.findViewById(R.id.roomNameforbookings)
        val dateTextView: TextView = itemView.findViewById(R.id.booking_dates_text)
        val occupantsTextView: TextView = itemView.findViewById(R.id.occupants_text)
        val roomImageView: ImageView = itemView.findViewById(R.id.room_image_view)
    }
}
