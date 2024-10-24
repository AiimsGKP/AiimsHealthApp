package com.example.aiimshealthapp

import android.content.Context
import android.content.SharedPreferences
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
import android.widget.ProgressBar
import android.widget.TextView
import com.example.aiimshealthapp.databinding.FragmentDummyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar
import java.util.Timer
import kotlin.concurrent.schedule

class DummyFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentDummyBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var isSensorPresent = false
    private var stepCount = 0
    private var previousStepCount = 0
    private var steps : List<Int> = emptyList()
    private lateinit var sharedPreferences: SharedPreferences
    private var stepGoal:String = "0"

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val user = currentUser?.let {
        User(it.displayName ?: "Unknown", it.email ?: "unknown@example.com")
    } ?: User("Unknown", "unknown@example.com")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDummyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sharedPreferences = requireContext().getSharedPreferences("stepCounterPrefs", Context.MODE_PRIVATE)

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            isSensorPresent = true

            // Get the previously stored initial step count
            previousStepCount = sharedPreferences.getInt("previousStepCount", -1)
        } else {
            binding.stepCounterTextView.text = "Step Counter sensor not available!"
            isSensorPresent = false
        }

        binding.username.text = user.username
        setDays(view)

        scheduleRun(14, 0){
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
                                    shiftedList[i - 1] = steps[i]
                                }
                                shiftedList[steps.size-2] = stepCount
                                steps = shiftedList.filterNotNull()
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
                                updateProgressBars()
                                updateSteps(0)
                            }
                        }
                    }
            }
        }
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
            // If this is the first time, store the initial step count
            if (previousStepCount == -1) {
                previousStepCount = totalSteps
                sharedPreferences.edit().putInt("previousStepCount", previousStepCount).apply()
            }

            // Calculate the steps taken today
            stepCount = totalSteps - previousStepCount
            updateSteps(stepCount)
            //step count


        }
    }

    private fun updateSteps(stepCount: Int){
        binding.stepCounterTextView.text = stepCount.toString()
        //calories and stepGoal
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
                            val goal = metrics?.get("stepGoal") as? String

                            steps = (metrics?.get("steps") as? List<Int>)!!
                            if(goal == "-1") {
                                binding.tvStepGoal.text = determineGoal(metrics)
                                stepGoal = determineGoal(metrics)
                            }
                            else{
                                binding.tvStepGoal.text = goal
                                stepGoal = goal.toString()
                            }
                            if (weight != null) {
                                val calories = calculateCaloriesBurnt(stepCount, weight.toFloat())
                                binding.tvCalories.text = "${calories.toInt().toString()}"
                            }
                            updateProgressBars()

                        }

                    }
                }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No need to handle accuracy changes for this sensor
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun calculateCaloriesBurnt(stepCount: Int, weightKg: Float): Float {
        val caloriesPerStep = 0.04f
        val totalCaloriesBurnt = stepCount * caloriesPerStep * (weightKg / 70f) // Assuming 70kg is the average weight
        return totalCaloriesBurnt
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

    private fun setDays(view: View) {
        val today = LocalDate.now().dayOfWeek
        val daysOfWeek = DayOfWeek.values()
        val daysList = (1..7).map { daysOfWeek[(today.ordinal + it) % daysOfWeek.size].toString()[0].toString() }
        listOf(
            R.id.day1,
            R.id.day2,
            R.id.day3,
            R.id.day4,
            R.id.day5,
            R.id.day6,
            R.id.day7
        ).forEachIndexed { index, id ->
            view.findViewById<TextView>(id)?.text = daysList[index]
        }
    }
    private fun updateProgressBars() {
        val daysProgress = listOf(
            binding.progressBar1,
            binding.progressBar2,
            binding.progressBar3,
            binding.progressBar4,
            binding.progressBar5,
            binding.progressBar6,
        )
        for ((item1, item2) in steps.zip(daysProgress)) {
            item2.max = stepGoal.toInt()
            item2.progress = item1
        }
        binding.progressBar7.max = stepGoal.toInt()
        binding.progressBar7.progress = stepCount
    }
    fun scheduleRun(hour: Int, minute: Int, task: () -> Unit) {
        val currentTime = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the scheduled time is before the current time, add one day
            if (this.timeInMillis <= currentTime.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delay = scheduledTime.timeInMillis - currentTime.timeInMillis

        Timer().schedule(delay) {
            task()
        }
    }

}
