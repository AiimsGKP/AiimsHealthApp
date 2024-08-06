package com.example.aiimshealthapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        val btnSignOut = findViewById<Button>(R.id.btnSignOut)
        findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar1).setProgress(30)
        findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar2).setProgress(70)
        findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar3).setProgress(60)

        findViewById<SemiCircleProgressBar>(R.id.semiCircleProgressBar1).setProgress(30)
        findViewById<SemiCircleProgressBar>(R.id.semiCircleProgressBar2).setProgress(70)
        findViewById<SemiCircleProgressBar>(R.id.semiCircleProgressBar3).setProgress(60)
        btnSignOut.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, LoginWithUsername::class.java)
            startActivity(intent)
            finish()
        }
    }
}