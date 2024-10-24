package com.example.aiimshealthapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Retrieve custom data passed from the activity
        val title = inputData.getString("NOTIFICATION_TITLE") ?: "Default Title"
        val message = inputData.getString("NOTIFICATION_MESSAGE") ?: "Default Message"
        // Show the notification with the custom title and message
        sendNotification(title, message)

        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val CHANNEL_ID = "workmanager_channel"

        // Create a notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "WorkManager Notification"
            val descriptionText = "Notification for scheduled work"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create and show the notification with custom title and message
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)  // Use custom title
            .setContentText(message)  // Use custom message
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt() // Generate a unique ID

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission handling here
                return
            }
            notify(notificationId, builder.build()) // Use unique ID
        }
    }

}

