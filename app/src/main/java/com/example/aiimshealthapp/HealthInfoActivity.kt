package com.example.aiimshealthapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aiimshealthapp.databinding.ActivityHealthInfoBinding

class HealthInfoActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding:ActivityHealthInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHealthInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        binding.apply {
            btn1.setOnClickListener(this@HealthInfoActivity)
            btn2.setOnClickListener(this@HealthInfoActivity)
            btn3.setOnClickListener(this@HealthInfoActivity)
            btn4.setOnClickListener(this@HealthInfoActivity)
            btn5.setOnClickListener(this@HealthInfoActivity)
            btn6.setOnClickListener(this@HealthInfoActivity)
            btn7.setOnClickListener(this@HealthInfoActivity)
            btn8.setOnClickListener(this@HealthInfoActivity)
            btn9.setOnClickListener(this@HealthInfoActivity)
            btn10.setOnClickListener(this@HealthInfoActivity)
            btn11.setOnClickListener(this@HealthInfoActivity)
            btn12.setOnClickListener(this@HealthInfoActivity)
        }


    }

    override fun onClick(view: View?) {
        val clickedBtn = view as Button
        val intent = Intent(this, HealthInfoPageActivity::class.java)
        intent.putExtra("topic", clickedBtn.text)
        startActivity(intent)
    }
}