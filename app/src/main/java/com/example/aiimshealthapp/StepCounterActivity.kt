package com.example.aiimshealthapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aiimshealthapp.databinding.ActivityStepCounterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar
import android.Manifest
import android.animation.ObjectAnimator
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class StepCounterActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityStepCounterBinding
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var isSensorPresent = false
    private var stepCount = 0
    private var previousStepCount = 0
    private var steps: List<Int> = emptyList()
    private lateinit var sharedPreferences: SharedPreferences
    private var stepGoal: String = "0"
    private val tag = "CHECK_RESPONSE"
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val ACTIVITY_RECOGNITION_REQUEST_CODE = 100
    private val currentDate = LocalDate.now()
    private val previousDate = currentDate.minusDays(1)
    private val user = currentUser?.let {
        User(it.displayName ?: "Unknown", it.email ?: "unknown@example.com")
    } ?: User("Unknown", "unknown@example.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepCounterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the Activity Recognition permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {

            // If not, request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQUEST_CODE
            )
        } else {
            // If permission is already granted, initialize step counter
            initializeStepCounter()
        }

        if (!sharedPreferences.contains(currentDate.toString())){
            if(sharedPreferences.contains(previousDate.toString())) {
                val previousDaySteps = sharedPreferences.getInt(previousDate.toString(), 0)
                Log.i(tag, "previous day steps:"+previousDaySteps.toString())
                executeMidnightTask(previousDaySteps)
            }
        }
        binding.stepCounterTextView.setOnClickListener{
//            executeMidnightTask(0)
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    // Callback for handling the permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeStepCounter()
            } else {
                onBackPressed()  // Go back to the previous page
            }
        }
    }

    private fun initializeStepCounter() {
        // Initialize Sensor Manager and Shared Preferences
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sharedPreferences = getSharedPreferences("StepsPrefs", Context.MODE_PRIVATE)
        Log.i(tag, sharedPreferences.getInt(currentDate.toString(), 0).toString())
        // Check for step counter sensor
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            isSensorPresent = true

            // Get previously stored initial step count
            previousStepCount = sharedPreferences.getInt("previousStepCount", -1)
        } else {
            binding.stepCounterTextView.text = "0"
            isSensorPresent = false
        }

        binding.username.text = user.username ?: ""
        setDays()
    }

    override fun onResume() {
        super.onResume()
        if (isSensorPresent) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isSensorPresent) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            sharedPreferences.edit().putInt("totalSteps", totalSteps).apply()
            Log.i(tag, "___" + totalSteps)
            // If this is the first time, store the initial step count
            if (previousStepCount == -1) {
                previousStepCount = totalSteps
                sharedPreferences.edit().putInt("previousStepCount", previousStepCount).apply()
            }
            // Calculate the steps taken today
            stepCount = totalSteps - previousStepCount
            sharedPreferences.edit().putInt(currentDate.toString(), stepCount).apply()
            updateSteps(stepCount)
        }
    }

    private fun executeMidnightTask(previousSteps:Int) {
        if (currentUser != null) {
            db.collection("metrics")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (document in documents) {
                            val documentId = document.id
                            val data = document.data
                            val metrics = (data["metrics"] as? Map<*, *>)?.toMutableMap()
                            steps = (metrics?.get("steps") as? List<Int>) ?: listOf()
                            val shiftedList = MutableList<Int?>(steps.size) { 0 }
                            for (i in 1 until steps.size) {
                                val date = currentDate.minusDays(i.toLong()-1)
                                shiftedList[steps.size - i] = sharedPreferences.getInt(date.toString(), 0)
                                Log.i(tag, date.toString() +"  " + sharedPreferences.getInt(date.toString(), 0))
                            }

                            steps = shiftedList.filterNotNull()
                            Log.i(tag, steps.toString())
                            metrics?.set("steps", steps)
                            previousStepCount = sharedPreferences.getInt("totalSteps", 0)
                            sharedPreferences.edit().putInt("previousStepCount", previousStepCount).apply()
                            val updates = mapOf("metrics" to metrics)
                            db.collection("metrics").document(documentId)
                                .update(updates)
                                .addOnSuccessListener {
                                    Log.d(tag, "Metrics updated successfully.")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(tag, "Error updating metrics", e)
                                }
                            updateSteps(0)
                        }
                    }
                }
        }
    }

    private fun updateSteps(stepCount: Int) {
        // Update UI
        binding.stepCounterTextView.text = stepCount.toString()
        if (currentUser != null) {
            db.collection("metrics")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (document in documents) {
                            val data = document.data
                            val metrics = data["metrics"] as? Map<String, Any>
                            val weight = metrics?.get("weight") as? String
                            val goal = metrics?.get("stepGoal") as? String

                            steps = (metrics?.get("steps") as? List<Int>) ?: emptyList()
                            stepGoal = if (goal == "-1") {
                                determineGoal(metrics)
                            } else {
                                goal.toString()
                            }
                            binding.tvStepGoal.text = stepGoal
                            sharedPreferences.edit().putInt("stepGoal", stepGoal.toInt()).apply()
                            if ( weight != null && weight.isNotEmpty()) {
                                val calories = calculateCaloriesBurnt(stepCount, weight.toFloat())
                                sharedPreferences.edit().putInt("calories", calories.toInt()).apply()
                                binding.tvCalories.text = "${calories.toInt()}"
                            }
                            binding.progressCircular.setMax(stepGoal.toInt())
                            binding.progressCircular.setProgress(stepCount)
                            updateProgressBars()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(tag, "Error fetching metrics", e)
                }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No need to handle accuracy changes
    }

    private fun setDays() {
        val today = LocalDate.now().dayOfWeek
        val daysOfWeek = DayOfWeek.values()
        val daysList = (1..7).map { daysOfWeek[(today.ordinal + it) % daysOfWeek.size].toString()[0].toString() }
        listOf(
            binding.day1,
            binding.day2,
            binding.day3,
            binding.day4,
            binding.day5,
            binding.day6,
            binding.day7
        ).forEachIndexed { index, textView ->
            textView.text = daysList[index]
        }
    }

    private fun updateProgressBars() {
        val daysProgress = listOf(
            binding.progressBar1,
            binding.progressBar2,
            binding.progressBar3,
            binding.progressBar4,
            binding.progressBar5,
            binding.progressBar6
        )
        for ((item1, item2) in steps.zip(daysProgress)) {
            item2.max = stepGoal.toInt()
            item2.progress = item1
            val progressAnimator = ObjectAnimator.ofInt(item2, "progress", 0, item1)
            progressAnimator.duration = 1000 // Set duration in milliseconds (2 seconds)
            progressAnimator.start()
        }
        binding.progressBar7.max = stepGoal.toInt()
        binding.progressBar7.progress = stepCount
    }

    private fun calculateCaloriesBurnt(stepCount: Int, weightKg: Float): Float {
        val caloriesPerStep = 0.04f
        return stepCount * caloriesPerStep * (weightKg / 70f)
    }

    private fun determineGoal(metrics: Map<String, Any>?): String {
        val age = (metrics?.get("age") as? String)?.toIntOrNull()
        return when {
            age in 5..17 -> "6000"
            age in 18..64 -> "3000"
            age != null && age >= 65 -> "3000"
            else -> "-1"
        }
    }


}
