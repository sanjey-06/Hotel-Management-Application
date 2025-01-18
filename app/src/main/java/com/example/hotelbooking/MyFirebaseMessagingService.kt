package com.example.hotelbooking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if the message contains a notification payload
        remoteMessage.data.isNotEmpty().let {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            sendNotification(title, body)
        }
    }

    private fun sendNotification(title: String?, body: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if the device is running Android O or higher for NotificationChannel support
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "checkout_channel", // Channel ID
                "Checkout Notifications", // Channel name
                NotificationManager.IMPORTANCE_HIGH // Set to high to make sure notifications show up
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Create notification
        val notification = NotificationCompat.Builder(this, "checkout_channel")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.hotellogo) // Replace with your app's notification icon
            .build()

        // Display the notification
        notificationManager.notify(0, notification)
    }
}
