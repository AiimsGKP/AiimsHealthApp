package com.example.aiimshealthapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.aiimshealthapp.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", 0)
        val title = intent.getStringExtra("notificationTitle")
        val text = intent.getStringExtra("notificationText")
        Log.i("CHECK_RESPONSE", "running 1")
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            Log.i("CHECK_RESPONSE", "running 2")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "my_channel_id"
                val channelName = "My Channel Name"
                val channelDescription = "Channel Description"

                val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                channel.description = channelDescription

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }

            // Build the notification
            val builder = NotificationCompat.Builder(context, "my_channel_id")
                .setSmallIcon(R.drawable.icon) // Replace with your icon
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Test with high priority
                .setAutoCancel(true) // Dismiss the notification when tapped

            // Show the notification
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, builder.build())
        } else {
            Log.i("CHECK_RESPONSE", "running 3")
        }
    }
}
