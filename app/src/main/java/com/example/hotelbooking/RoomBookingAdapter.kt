package com.example.hotelbooking

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RoomBookingAdapter(
    private val bookingsWithDates: List<Pair<List<Room>, Pair<String, String>>>
) : RecyclerView.Adapter<RoomBookingAdapter.RoomViewHolder>() {

    // Flatten the bookingsWithDates list into a single list of rooms
    private val flattenedRoomList: List<Room> = bookingsWithDates.flatMap { (rooms, _) -> rooms }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        // Inflate the layout for each room item in the RecyclerView
        val view = LayoutInflater.from(parent.context).inflate(R.layout.roomlist, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        // Get the current booking and room
        val (rooms, dates) = bookingsWithDates.find { it.first.contains(flattenedRoomList[position]) }!!
        val (startDate, endDate) = dates

        // Get the room for the current position in the flattened list
        val room = flattenedRoomList[position]

        // Set the room name, number of occupants, and date range for this room
        holder.roomTypeTextView.text = room.name
        holder.dateTextView.text = "$startDate to $endDate"
        holder.occupantsTextView.text = "Occupants: ${room.numberofoccupants}"

        // Load the room image using Glide
        Glide.with(holder.itemView.context)
            .load(room.imageURL)
            .placeholder(R.drawable.hotellogo)
            .error(R.drawable.homeicon)
            .into(holder.roomImageView)

        // Set click listener for Raise Complaint button
        holder.raiseComplaintButton.setOnClickListener {
            showComplaintDialog(holder.itemView.context)
        }

        // Check if the current room is the last one in its booking
        val isLastRoomInBooking = rooms.indexOf(room) == rooms.size - 1

        // Set visibility for the separator line
        if (isLastRoomInBooking) {
            // Show the separator line only for the last room of each booking
            holder.separatorLine.visibility = View.VISIBLE
        } else {
            // Hide the separator line for other rooms
            holder.separatorLine.visibility = View.GONE
        }
    }



    override fun getItemCount(): Int = flattenedRoomList.size

    private fun showComplaintDialog(context: Context) {
        // Create a dialog for entering a complaint
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Raise a Complaint")

        // Inflate the layout for the complaint dialog
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null)
        val complaintEditText: EditText = dialogView.findViewById(R.id.complaint_edit_text)
        val sendButton: Button = dialogView.findViewById(R.id.send_complaint_button)

        builder.setView(dialogView)

        val dialog = builder.create()

        // Handle the Send button click
        sendButton.setOnClickListener {
            val complaintText = complaintEditText.text.toString()
            if (complaintText.isNotEmpty()) {
                // Send the complaint to the database (this part will be implemented next)
                sendComplaintToDatabase(complaintText, context)
                dialog.dismiss()
            } else {
                // Show a message if the complaint is empty
                Toast.makeText(context, "Please enter a complaint", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        dialog.show()
    }

    private fun sendComplaintToDatabase(complaint: String, context: Context) {
        // Get the current user's ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Check if the user is logged in
        if (userId != null) {
            // Query the database to find the booking associated with this user
            val bookingsReference = FirebaseDatabase.getInstance().getReference("bookings")
            bookingsReference.orderByChild("userId").equalTo(userId)  // Assuming bookings have userId as a field

            bookingsReference.get().addOnSuccessListener { snapshot ->
                // Iterate over all the bookings for the user (you can adjust the query logic if needed)
                for (bookingSnapshot in snapshot.children) {
                    val bookingId = bookingSnapshot.key // The key of each booking node is the bookingId

                    if (bookingId != null) {
                        // Store the complaint under the specific booking
                        storeComplaintUnderBooking(bookingId, complaint, context)
                        return@addOnSuccessListener // Exit after storing the complaint
                    }
                }

                // If no booking was found
                Toast.makeText(context, "No booking found for the user", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                // Handle failure in querying the database
                Toast.makeText(context, "Failed to fetch booking: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeComplaintUnderBooking(bookingId: String, complaint: String, context: Context) {
        // Create a database reference to the complaints under the bookingId
        val databaseReference = FirebaseDatabase.getInstance().getReference("bookings")
            .child(bookingId)
            .child("complaints")

        // Generate a unique ID for the complaint
        val complaintId = databaseReference.push().key

        if (complaintId != null) {
            val complaintData = hashMapOf(
                "userId" to FirebaseAuth.getInstance().currentUser?.uid,
                "complaintText" to complaint,
                "timestamp" to System.currentTimeMillis()
            )

            // Save the complaint to the database
            databaseReference.child(complaintId).setValue(complaintData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Complaint sent successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Failed to send complaint: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // ViewHolder to hold the room details
    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomTypeTextView: TextView = itemView.findViewById(R.id.roomNameforbookings)
        val dateTextView: TextView = itemView.findViewById(R.id.booking_dates_text)
        val occupantsTextView: TextView = itemView.findViewById(R.id.occupants_text)
        val roomImageView: ImageView = itemView.findViewById(R.id.room_image_view)
        val raiseComplaintButton: Button = itemView.findViewById(R.id.raise_complaint_button)
        val separatorLine: View = itemView.findViewById(R.id.separator_line)  // Reference to the separator line
    }
}
