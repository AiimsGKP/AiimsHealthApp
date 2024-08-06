package com.example.aiimshealthapp

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SocioDemographic : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_socio_demographic)

        val btnNext = findViewById<Button>(R.id.btnNext)
        btnNext.setOnClickListener {
            setContentView(R.layout.activity_anthropometry)

            val btnPrevious = findViewById<Button>(R.id.btnPrevious)
            btnPrevious.setOnClickListener {
                setContentView(R.layout.activity_socio_demographic)
            }
        }
    }
}