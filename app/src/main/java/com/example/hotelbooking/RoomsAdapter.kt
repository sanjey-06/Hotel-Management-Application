package com.example.hotelbooking

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RoomsAdapter(
    private val rooms: MutableList<Room>, // Mutable list of rooms to allow updates
    private val startDate: String?,
    private val endDate: String?,
    private val updateRoomCount: (Int) -> Unit, // Callback function to update the total rooms count
    private val onSelectedRoomsChanged: (List<Room>) -> Unit,// Callback to notify selected rooms change
    private var maxRoomsAllowed: Int

) : RecyclerView.Adapter<RoomsAdapter.RoomViewHolder>() {



    inner class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val roomId: TextView = view.findViewById(R.id.roomId)
        val roomName: TextView = view.findViewById(R.id.roomName)
        val roomPrice: TextView = view.findViewById(R.id.roomPrice)
        val roomImage: ImageView = view.findViewById(R.id.roomImage)
        val decrementButton: AppCompatImageButton = view.findViewById(R.id.decrementRoomButton)
        val incrementButton: AppCompatImageButton = view.findViewById(R.id.incrementRoomButton)
        val roomQuantityTextView: TextView = view.findViewById(R.id.roomQuantity)
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

        Glide.with(holder.itemView.context)
            .load(room.imageURL) // Load image
            .into(holder.roomImage)

        // Set initial quantity in the TextView
        holder.roomQuantityTextView.text = room.numberofrooms.toString()

        // Decrement button click listener
        holder.decrementButton.setOnClickListener {
            if (room.numberofrooms > 1) { // Prevent number going below 1
                val updatedRoom = room.copy(numberofrooms = room.numberofrooms - 1)
                rooms[position] = updatedRoom // Update the room in the list
                notifyItemChanged(position) // Notify adapter to update the UI
                updateRoomCount(getTotalRoomsSelected()) // Update total rooms count


                // Update the selected rooms
                onSelectedRoomsChanged(getSelectedRooms()) // Notify activity of the change
                // Debugging the price and number of rooms
                Log.d("RoomDetails", "Room updated: ${room.name}, Quantity: ${updatedRoom.numberofrooms}, Total Price: ${updatedRoom.price * updatedRoom.numberofrooms}")
            }
        }

        // Increment button click listener
        holder.incrementButton.setOnClickListener {
            if (getTotalRoomsSelected() < maxRoomsAllowed) {
                val updatedRoom = room.copy(numberofrooms = room.numberofrooms + 1)
                rooms[position] = updatedRoom // Update the room in the list
                notifyItemChanged(position) // Notify adapter to update the UI
                updateRoomCount(getTotalRoomsSelected()) // Update total rooms count
                onSelectedRoomsChanged(getSelectedRooms()) // Notify activity of the change
            } else {
                Toast.makeText(holder.itemView.context, "Max rooms reached", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun getTotalRoomsSelected(): Int {
        return rooms.sumOf { it.numberofrooms } // Sum up all the numberofrooms values
    }

    private fun getSelectedRooms(): List<Room> {
        return rooms.filter { it.numberofrooms > 0 } // Only return rooms with selected quantity > 0
    }

    override fun getItemCount(): Int = rooms.size
}