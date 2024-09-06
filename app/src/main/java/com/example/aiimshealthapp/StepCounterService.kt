package com.example.aiimshealthapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Build
import androidx.core.app.NotificationCompat

class StepCounterService : Service() {

    override fun onCreate() {
        super.onCreate()

        // Creating Notification Channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "step_counter_channel",
                "Step Counter Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, "step_counter_channel")
            .setContentTitle("Step Counter Active")
            .setContentText("Counting your steps in the background")
            .setSmallIcon(R.drawable.icon)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Logic for counting steps should be implemented here
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
