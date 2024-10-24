package com.example.aiimshealthapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ResetStepsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mainIntent = Intent(context, StepCounterActivity::class.java)
        mainIntent.action = Intent.ACTION_MAIN  // Set action explicitly
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)  // Important to start activity from receiver
        context.startActivity(mainIntent)
    }
}


