package com.example.aiimshealthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    private val mAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (mAuth.currentUser != null) {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
            return
        }
        else{
            val intent = Intent(this, GetStartedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}