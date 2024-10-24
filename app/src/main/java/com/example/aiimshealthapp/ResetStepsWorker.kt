package com.example.aiimshealthapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate

class ResetStepsWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val sharedPreferences: SharedPreferences =
            applicationContext.getSharedPreferences("StepsPrefs", Context.MODE_PRIVATE)

        val currentDate = LocalDate.now()
        val steps = sharedPreferences.getInt(currentDate.toString(), 0)
        Log.i("CHECK_RESPONSE",  "schedule working with steps given $steps")
        return Result.success()
    }
}
