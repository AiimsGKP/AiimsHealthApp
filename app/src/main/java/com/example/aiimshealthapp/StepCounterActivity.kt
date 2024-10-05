package com.example.aiimshealthapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.hardware.*
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class StepCounterActivity : AppCompatActivity(), SensorEventListener {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private lateinit var tv_stepsTaken: TextView
    private val tag = "CHECK_RESPONSE"
    private lateinit var progressBar : FullCircleProgressBar
    private var stepGoal = 6000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_counter)
        tv_stepsTaken = findViewById(R.id.stepCounterTextView)
        progressBar = findViewById<FullCircleProgressBar>(R.id.progressCircular)

        getGoal()
        loadData()
        findViewById<Button>(R.id.btnSetGoal).setOnClickListener {
            showSetGoalDialog()
        }
        val intent = Intent(this, StepCounterService::class.java)
        startService(intent)
    }

    private fun getGoal() {
        if(currentUser != null){
            db.collection("metrics")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (document in documents) {
                            val data = document.data
                            val metrics = data["metrics"] as? Map<String, Any>
                            var goal = metrics?.get("stepGoal") as? String
                            val age = metrics?.get("age") as? String
                            val name = metrics?.get("firstName") as? String

                            findViewById<TextView>(R.id.username).text = name.toString()
                            if (age != null && goal.toString() == "-1") {
                                if(age.toInt() >= 5 && age.toInt() <= 17){
                                    goal = "6000"
                                }
                                else if(age.toInt() >= 18 && age.toInt()<=64){
                                    goal = "3000"
                                }
                                else if(age.toInt() >= 65){
                                    goal = "3000"
                                }
                                findViewById<TextView>(R.id.tvStepGoal).text = "${goal.toString()} Steps"
                            }
                            if (goal != null) {
                                progressBar.setMax(goal.toInt())
                                progressBar.setProgress(totalSteps.toInt() - previousTotalSteps.toInt())
                            }
                        }
                    }
                }
        }
    }

    private fun setGoal(goal:String){
        if(currentUser != null){
            db.collection("metrics")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (document in documents) {
                            val documentId = document.id
                            val data = document.data
                            val metrics = (data["metrics"] as? Map<*, *>)?.toMutableMap()
                            metrics?.set("stepGoal", goal) // Example of updating weight

                            // Update the document with the new metrics map
                            val updates = mapOf("metrics" to metrics)
                            db.collection("metrics").document(documentId)
                                .update(updates)
                                .addOnSuccessListener {
                                    Log.d(tag, "Metrics updated successfully.")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(tag, "Error updating metrics", e)
                                }
                        }
                    }
                }
        }
    }

    private fun showSetGoalDialog() {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_goal, null)
        val editTextGoal = dialogView.findViewById<EditText>(R.id.editTextGoal)

        // Build the dialog
        AlertDialog.Builder(this)
            .setTitle("Set Step Goal")
            .setView(dialogView)
            .setPositiveButton("Set") { dialog, _ ->
                val goalString = editTextGoal.text.toString()
                val goal = goalString.toIntOrNull() ?: stepGoal // Use current stepGoal if input is invalid
                if (goal > 0) {
                    stepGoal = goal
                    progressBar.setMax(stepGoal)
                    findViewById<TextView>(R.id.tvStepGoal).text = "${stepGoal} Steps"
                    setGoal(stepGoal.toString())
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        running = true
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            totalSteps = event!!.values[0]
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            tv_stepsTaken.text = currentSteps.toString()
            progressBar.setProgress(currentSteps)
            if(currentUser!=null) {
                db.collection("metrics")
                    .whereEqualTo("user.email", currentUser.email)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents != null) {
                            for(document in documents){
                                val data = document.data
                                val metrics = data["metrics"] as? Map<String, Any>
                                val weight = metrics?.get("weight") as? String
                                if (weight != null) {
                                    val calories = calculateCaloriesBurnt(currentSteps, weight.toFloat())
                                    findViewById<TextView>(R.id.tvCalories).text = "${calories.toInt().toString()}Cal"
                                }
                            }

                        }
                    }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No need to implement this for step counting
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
        Log.d(tag, "$savedNumber")
        previousTotalSteps = savedNumber


    }

    fun calculateCaloriesBurnt(stepCount: Int, weightKg: Float): Float {
        // Average calories burnt per step (varies by weight and intensity)
        val caloriesPerStep = 0.04f

        // Calculate total calories burnt
        val totalCaloriesBurnt = stepCount * caloriesPerStep * (weightKg / 70f) // Assuming 70kg is the average weight

        return totalCaloriesBurnt
    }

    private fun setupDailyReset() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ResetStepsReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }


}
