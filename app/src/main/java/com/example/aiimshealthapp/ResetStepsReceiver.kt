package com.example.aiimshealthapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ResetStepsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val sharedPreferences = it.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putFloat("key1", 0f) // Reset previousTotalSteps to 0
            editor.apply()
        }
    }
}
