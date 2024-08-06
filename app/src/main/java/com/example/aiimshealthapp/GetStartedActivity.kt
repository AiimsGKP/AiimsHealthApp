package com.example.aiimshealthapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class GetStartedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)
        val button = findViewById<Button>(R.id.btnGetStarted)
        button.setOnClickListener {
            val intent = Intent(this, LoginWithUsername::class.java)
            startActivity(intent)
        }
    }
}