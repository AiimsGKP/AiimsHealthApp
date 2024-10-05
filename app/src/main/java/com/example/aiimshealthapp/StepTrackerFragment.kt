package com.example.aiimshealthapp

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar


class StepTrackerFragment : Fragment(), SensorEventListener {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    val metricsData = MetricsData()
    val user = currentUser?.let {
        User(username = it.displayName ?: "Unknown", email = it.email ?: "unknown@example.com")
    } ?: User(username = "Unknown", email = "unknown@example.com") // Provide default values

    // Create Metrics instance
    var metrics = Metrics(user = user, metrics = metricsData)
    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private lateinit var tv_stepsTaken: TextView
    private val tag = "CHECK_RESPONSE"
    private lateinit var progressBar : FullCircleProgressBar
    private var stepGoal = 6000
    private var currentSteps = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Call the function to update the TextViews
        setDays(view)
        updateProgressBars(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_step_tracker, container, false)
        tv_stepsTaken = view.findViewById(R.id.stepCounterTextView)
        progressBar = view.findViewById(R.id.progressCircular)

        getGoal()
        loadData()
        setupDailyReset()


        view.findViewById<LinearLayout>(R.id.btnSetGoal).setOnClickListener {
            showSetGoalDialog(view)
        }

        // Start the StepCounterService
        val intent = Intent(requireContext(), StepCounterService::class.java)
        requireContext().startService(intent)

        return view
    }
    private fun getGoal() {
        if (currentUser != null) {
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
                            val name = currentUser.displayName
                            view?.findViewById<TextView>(R.id.username)?.text = name.toString()

                            if (!age.isNullOrEmpty() && goal.toString() == "-1") {
                                // Attempt to parse age only if it's not empty
                                val ageInt = age.toIntOrNull() // Safely parse the string to an Int
                                if (ageInt != null) {
                                    goal = when {
                                        ageInt in 5..17 -> "6000"
                                        ageInt in 18..64 -> "3000"
                                        ageInt >= 65 -> "3000"
                                        else -> goal
                                    }
                                }
                            }

                            if (goal != null) {
                                progressBar.setMax(goal.toInt())
                                progressBar.setProgress(totalSteps.toInt() - previousTotalSteps.toInt())
                                view?.findViewById<TextView>(R.id.tvStepGoal)?.text = "$goal"
                                stepGoal = goal.toInt()
                            }
                        }
                    }
                }
        }
    }

    private fun setGoal(goal: String) {
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
                            metrics?.set("stepGoal", goal)

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

    private fun showSetGoalDialog(view: View) {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_goal, null)
        val editTextGoal = dialogView.findViewById<EditText>(R.id.editTextGoal)

        // Build the dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Set Step Goal")
            .setView(dialogView)
            .setPositiveButton("Set") { dialog, _ ->
                val goalString = editTextGoal.text.toString()
                val goal = goalString.toIntOrNull() ?: stepGoal
                if (goal > 0) {
                    stepGoal = goal
                    progressBar.setMax(stepGoal)
                    progressBar.setProgress(totalSteps.toInt() - previousTotalSteps.toInt())
                    view.findViewById<TextView>(R.id.tvStepGoal)?.text = "$stepGoal"
                    setGoal(stepGoal.toString())
                    updateProgressBars(view)
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
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(requireContext(), "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            if (event != null && event.values.isNotEmpty()) {
                totalSteps = event.values[0]
                currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()

                tv_stepsTaken.text = currentSteps.toString()
                progressBar.setProgress(currentSteps)

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
                                    if (!weight.isNullOrEmpty()) {
                                        val calories = calculateCaloriesBurnt(currentSteps, weight.toFloat())
                                        view?.findViewById<TextView>(R.id.tvCalories)?.text =
                                            "${calories.toInt()}"
                                    } else {
                                        // Optionally handle the case where weight is empty
                                        Log.w(tag, "Weight is not provided or empty.")
                                        view?.findViewById<TextView>(R.id.tvCalories)?.text = "N/A" // Or any other appropriate message
                                    }
                                    getGoal()
                                    view?.findViewById<ProgressBar>(R.id.progressBar7)?.apply {
                                        max = stepGoal
                                        progress = currentSteps // Progress for yesterday
                                    }

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
        val sharedPreferences = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
        previousTotalSteps = savedNumber
    }

    fun calculateCaloriesBurnt(stepCount: Int, weightKg: Float): Float {
        val caloriesPerStep = 0.04f
        val totalCaloriesBurnt = stepCount * caloriesPerStep * (weightKg / 70f) // Assuming 70kg is the average weight
        return totalCaloriesBurnt
    }

    private fun setupDailyReset() {

//        updateSteps(currentSteps)
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ResetStepsReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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

    private fun setDays(view: View){

        val today = LocalDate.now().dayOfWeek
        val daysOfWeek = DayOfWeek.values()

        // Create a list starting from tomorrow's day of week
        val tomorrowIndex = (today.ordinal + 1) % daysOfWeek.size
        val daysList = mutableListOf<String>()

        // Add days starting from tomorrow till today's day
        for (i in tomorrowIndex until daysOfWeek.size) {
            daysList.add(daysOfWeek[i].toString())
        }
        for (i in 0..today.ordinal) {
            daysList.add(daysOfWeek[i].toString())
        }
        view.findViewById<TextView>(R.id.day1)?.text = daysList[0][0].toString()
        view.findViewById<TextView>(R.id.day2)?.text = daysList[1][0].toString()
        view.findViewById<TextView>(R.id.day3)?.text = daysList[2][0].toString()
        view.findViewById<TextView>(R.id.day4)?.text = daysList[3][0].toString()
        view.findViewById<TextView>(R.id.day5)?.text = daysList[4][0].toString()
        view.findViewById<TextView>(R.id.day6)?.text = daysList[5][0].toString()
        view.findViewById<TextView>(R.id.day7)?.text = daysList[6][0].toString()

    }
    private fun updateProgressBars(view: View) {
        // Define the values corresponding to the days of the week
        if (currentUser != null) {
            db.collection("metrics")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (document in documents) {
                            val data = document.data
                            val metrics = data["metrics"] as? Map<String, Any>
                            val steps = metrics?.get("steps") as? List<Int>
                            val values: List<Int> =
                                steps?.toList() ?: List(7) { 0 }
                            if (steps != null) {
                                view.findViewById<ProgressBar>(R.id.progressBar1)?.apply {
                                    max = stepGoal
                                    progress = steps[0] // Progress for 7 days ago
                                }
                                view.findViewById<ProgressBar>(R.id.progressBar2)?.apply {
                                    max = stepGoal
                                    progress = steps[1] // Progress for 6 days ago
                                }
                                view.findViewById<ProgressBar>(R.id.progressBar3)?.apply {
                                    max = stepGoal
                                    progress = steps[2] // Progress for 5 days ago
                                }
                                view.findViewById<ProgressBar>(R.id.progressBar4)?.apply {
                                    max = stepGoal
                                    progress = steps[3] // Progress for 4 days ago
                                }
                                view.findViewById<ProgressBar>(R.id.progressBar5)?.apply {
                                    max = stepGoal
                                    progress = steps[4] // Progress for 3 days ago
                                }
                                view.findViewById<ProgressBar>(R.id.progressBar6)?.apply {
                                    max = stepGoal
                                    progress = steps[5] // Progress for 2 days ago
                                }
                            }

                        }
                    }
                }
        }


    }

    private fun updateSteps(newSteps: Int) {
        // Assuming 'metrics' is an instance of Metrics
        val updatedSteps = metrics.metrics.steps.toMutableList() // Access steps through metrics

        // Shift values to the left (remove the oldest day)
        if (updatedSteps.size >= 7) {
            updatedSteps.removeAt(0)
        }

        // Add the new steps for today
        updatedSteps.add(newSteps)
        Log.i(tag,"updatedSteps - ${updatedSteps}")
        // Update the metrics with the new list
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
                            metrics?.set("steps", updatedSteps)

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



}