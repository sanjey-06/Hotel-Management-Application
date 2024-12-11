package com.example.hotelbooking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Add Glide dependency


class RoomsAdapter(private val rooms: List<Room>) : RecyclerView.Adapter<RoomsAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val roomId: TextView = view.findViewById(R.id.roomId)
        val roomName: TextView = view.findViewById(R.id.roomName)
        val roomPrice: TextView = view.findViewById(R.id.roomPrice)
        val roomImage: ImageView = view.findViewById(R.id.roomImage) // ImageView reference
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rooms_item, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.roomId.text = "Room ID: ${room.id}"
        holder.roomName.text = room.name
        holder.roomPrice.text = "$${room.price}/night"

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(room.imageURL) // Load the image URL
            .into(holder.roomImage)
    }

    override fun getItemCount(): Int = rooms.size
}

