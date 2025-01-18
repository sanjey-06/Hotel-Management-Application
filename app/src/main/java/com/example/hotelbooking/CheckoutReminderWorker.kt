package com.example.hotelbooking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class CheckoutReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            // Get FCM Token and save it to Firebase
            FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        userRef.child("fcmToken").setValue(token).addOnSuccessListener {
                            Log.d("notfchecker", "FCM Token stored successfully for userId: $userId")
                        }
                    } else {
                        Log.e("notfchecker", "Failed to get FCM Token")
                    }
                }

            // Fetch user's booking info
            val bookingsRef = FirebaseDatabase.getInstance().getReference("bookings")
            bookingsRef.orderByChild("userId").equalTo(userId).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        snapshot?.children?.forEach { bookingSnapshot ->
                            val endTimestamp = bookingSnapshot.child("timestamp").value as? Long
                            val bookingId = bookingSnapshot.key // This should give the unique booking ID

                            if (endTimestamp != null && bookingId != null) {
                                val currentTime = System.currentTimeMillis()
                                val reminderTime = endTimestamp - 30 * 60 * 1000 // 30 minutes before end time

                                Log.d("notfchecker", "Current Time: $currentTime, Reminder Time: $reminderTime, End Time: $endTimestamp")

                                // Send "Booking Ending Soon" notification if the current time is within 30 seconds of the reminder time
                                if (currentTime in (reminderTime - 30000)..(reminderTime + 30000)) { // ±30 seconds range for precision
                                    sendNotification("Booking Reminder", "Your booking is ending soon!")
                                }

                                // Send "Booking Ended" notification when the booking time has passed
                                if (currentTime in (endTimestamp - 30000)..(endTimestamp + 30000)) { // ±30 seconds range for precision
                                    sendNotification("Booking Ended", "Your booking has ended. Please check out.")

                                    // Remove the booking data after the time ends
                                    bookingsRef.child(bookingId).removeValue()
                                        .addOnSuccessListener {
                                            Log.d("notfchecker", "Booking data removed successfully for bookingId: $bookingId")
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("notfchecker", "Failed to remove booking data: ${exception.message}")
                                        }

                                    // Remove the FCM token from the user's record after the booking ends
                                    userRef.child("fcmToken").removeValue()
                                        .addOnSuccessListener {
                                            Log.d("notfchecker", "FCM Token removed successfully for userId: $userId")
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("notfchecker", "Failed to remove FCM Token: ${exception.message}")
                                        }
                                }
                            }
                        }
                    } else {
                        Log.e("notfchecker", "Error fetching booking info: ${task.exception?.message}")
                    }
                }
        } else {
            Log.d("notfchecker", "No authenticated user found.")
            return Result.failure()
        }

        return Result.success()
    }

    private fun sendNotification(title: String, body: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "checkout_channel",
                "Checkout Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "checkout_channel")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.hotellogo) // Replace with your app's notification icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(title.hashCode(), notification)
        Log.d("notfchecker", "Notification sent: $title - $body")
    }
}
