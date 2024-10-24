package com.example.aiimshealthapp

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.aiimshealthapp.databinding.ActivityHydrationReminderBinding
import com.example.aiimshealthapp.models.Hydration
import com.example.aiimshealthapp.models.Timer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit

class HydrationReminderActivity : AppCompatActivity(), OnTimerRemoveListener {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private lateinit var binding: ActivityHydrationReminderBinding // Declare a binding variable
    private lateinit var timerAdapter: HydrationAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val timerList: MutableList<Timer> = mutableListOf()
    private val tag = "CHECK_RESPONSE"
    private val workRequestIds: MutableMap<String, UUID> = mutableMapOf() // Map to hold timer titles and their WorkRequest IDs

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        sharedPreferences = getSharedPreferences("reminders", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        checkNotificationPermission()
        binding = ActivityHydrationReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        loadTimers()
        binding.addTimer.setOnClickListener {
            addTimer()
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


        binding.hydrationReminder.isChecked = sharedPreferences.getBoolean("hydration", true)
        toggleSwitch(sharedPreferences.getBoolean("hydration", true))

        animateDrawable(binding.onImage, R.drawable.water_glass_on)
        animateDrawable(binding.offImage, R.drawable.water_glass_off)

        binding.hydrationReminder.setOnCheckedChangeListener { _, isChecked ->
            toggleSwitch(isChecked)
        }
        scheduleActivity()
    }

    private fun toggleSwitch(isChecked : Boolean){
        if(isChecked){
            binding.hydrationText.text = "On"
            binding.recyclerView.visibility = View.VISIBLE
            binding.disabledTimer.visibility = View.GONE
            if(timerList.size == 0) {
                binding.emptyTimer.visibility = View.VISIBLE
            }
            else{
                binding.emptyTimer.visibility = View.GONE
            }
            editor.putBoolean("hydration", true)
            editor.apply()
        }else{
            binding.hydrationText.text = "Off"
            binding.recyclerView.visibility = View.GONE
            binding.disabledTimer.visibility = View.VISIBLE
            binding.emptyTimer.visibility = View.GONE
            for(i in timerList){
                cancelScheduledNotification(i.cardViewId)
            }
            editor.putBoolean("hydration", false)
            editor.apply()
        }
    }

    private fun animateDrawable(imageView: ImageView, @DrawableRes drawableId: Int) {
        // Load the vector drawable
        val drawable = ContextCompat.getDrawable(imageView.context, drawableId)

        // Check if the drawable is an AnimatedVectorDrawable
        if (drawable is AnimatedVectorDrawable) {
            imageView.setImageDrawable(drawable)
            drawable.start()
        } else if (drawable is VectorDrawable) {
            // If the drawable is a VectorDrawable, add animation programmatically
            imageView.setImageDrawable(drawable)

            // Assume the group name to animate is "water_wave_group", you can generalize this based on the specific drawable
            val objectAnimator = ObjectAnimator.ofFloat(imageView, "translationY", 0f, 15f)
            objectAnimator.duration = 1000
            objectAnimator.repeatCount = ObjectAnimator.INFINITE
            objectAnimator.repeatMode = ObjectAnimator.REVERSE
            objectAnimator.start()
        } else {
            // If it's not a vector, just set the image normally
            imageView.setImageDrawable(drawable)
        }
    }

    override fun onRemove(timer: Timer) {
        cancelScheduledNotification(timer.cardViewId)
        timerList.remove(timer) // Remove the timer from the list
        timerAdapter.notifyDataSetChanged()
        updateDatabase()
    }

    override fun onUpdate(timer: Timer) {
        val view = layoutInflater.inflate(R.layout.hydration_dialog, null)
        val timePicker = view.findViewById<TimePicker>(R.id.timePicker)

        // Create the TimePickerDialog using the custom view
        val timePickerDialog = AlertDialog.Builder(this)
            .setTitle("Update Timer")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                val hourOfDay = timePicker.hour
                val minute = timePicker.minute
                val amPm = if (hourOfDay >= 12) "pm" else "am"
                val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12

                timer.hour = hour.toString()
                timer.minute = minute.toString()
                timer.amPm = amPm
                updateDatabase()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        // Set custom colors for dialog buttons
        timePickerDialog.setOnShowListener {
            timePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.neutral_dark_1))
            timePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.neutral_dark_1))
        }

        timePickerDialog.show()
        timerAdapter.notifyDataSetChanged()
    }

    override fun onDisable(timer: Timer) {
        if(!timer.activated){
            cancelScheduledNotification(timer.cardViewId)
        }
        else{
            scheduleDailyNotification(timer.cardViewId, timer.hour.toInt(), timer.minute.toInt())
        }
        updateDatabase()
    }

    private fun checkNotificationPermission() {
        // Only request permission if the app is running on Android 13 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Request the POST_NOTIFICATIONS permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("CHECK_RESPONSE", "Notification permission granted")
            } else {
                Log.e("CHECK_RESPONSE", "Notification permission denied")
            }
        }
    }

    private fun loadTimers(){
        if(currentUser!= null) {
            db.collection("hydration")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if(querySnapshot == null){
                        updatePage()
                    }
                    else{
                        for (document in querySnapshot) {
                            val data = document.data
                            val timerss = data["timers"] as? List<Map<String, Any>>
                            if (timerss != null) {
                                for(timer in timerss){
                                    val activated = (timer["activated"] as? Boolean) ?: true
                                    val id = timer["cardViewId"] as? String
                                    val hour = timer["hour"] as? String
                                    val minute = timer["minute"] as? String
                                    val amPm = timer["amPm"] as? String

                                    timerList.add(Timer("",activated, id.toString(), hour.toString(), minute.toString(), amPm.toString()))
                                }
                                updatePage()
                            }

                        }
                    }
                }
        }
    }
    private fun updatePage(){
        if(binding.hydrationReminder.isChecked){
            if (timerList.size == 0){
                binding.emptyTimer.visibility = View.VISIBLE
            }
            else{
                binding.emptyTimer.visibility = View.GONE
            }
        }
        timerAdapter = HydrationAdapter(timerList, this)
        binding.recyclerView.adapter = timerAdapter
        scheduleActivity()
    }

    private fun addTimer() {
        // Inflate the custom layout
        val view = layoutInflater.inflate(R.layout.hydration_dialog, null)
        val timePicker = view.findViewById<TimePicker>(R.id.timePicker)

        // Create the TimePickerDialog using the custom view
        val timePickerDialog = AlertDialog.Builder(this)
            .setTitle("Select Time")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                val hourOfDay = timePicker.hour
                val minute = timePicker.minute
                val amPm = if (hourOfDay >= 12) "pm" else "am"
                val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12

                val timer = Timer("", true, "", hour.toString(), minute.toString(), amPm)
                timerList.add(timer)
                updateDatabase()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        // Set custom colors for dialog buttons
        timePickerDialog.setOnShowListener {
            timePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.neutral_dark_1))
            timePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.neutral_dark_1))
        }

        timePickerDialog.show()
    }

    fun assignCardViewIds() {
        for (index in timerList.indices) {
            // Assign a unique ID, for example, "timer_<index>"
            timerList[index].cardViewId = "$index"
        }
    }
    private fun updateDatabase() {
        if (currentUser != null) {
            val username = currentUser.email.toString().substringBefore("@")
            val user = User(username ?: "Unknown User", currentUser.email ?: "No Email")
            val userId = auth.currentUser?.uid ?: "unknown_user"
            assignCardViewIds()
            val hydration = Hydration(user, timerList)
            db.collection("hydration")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        db.collection("hydration").document(userId).set(hydration)
                            .addOnSuccessListener {
                                updatePage()
                            }
                    } else {
                        val document = querySnapshot.documents.first()
                        val documentId = document.id
                        val newData = hydration.toMap()
                        db.collection("hydration").document(documentId)
                            .set(newData)
                            .addOnSuccessListener {
                                updatePage()
                            }
                    }
                }
        }
    }

    private fun scheduleActivity() {
        if (currentUser != null) {
            val username = currentUser.email.toString().substringBefore("@")
            val user = User(username ?: "Unknown User", currentUser.email ?: "No Email")
            val userId = auth.currentUser?.uid ?: "unknown_user"
            db.collection("hydration")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot != null) {
                        for (document in querySnapshot) {
                            val data = document.data
                            val timerss = data["timers"] as? List<Map<String, Any>>
                            if (timerss != null) {
                                for (timer in timerss) {
                                    val title = timer["title"] as? String
                                    val id = timer["cardViewId"] as? String
                                    val activated = (timer["activated"] as? Boolean) ?: true
                                    var hour = timer["hour"] as? String
                                    val minute = timer["minute"] as? String
                                    val amPm = timer["amPm"] as? String
                                    if(amPm == "pm"){
                                        if (hour != null && hour.toInt() < 12) {
                                            hour = (hour.toInt() + 12).toString()
                                        }
                                    }
                                    else if(amPm == "am"){
                                        if (hour != null && hour.toInt() == 12) {
                                            hour = "0"
                                        }
                                    }
                                    if(activated){
                                        if (hour != null && minute != null && title != null) {
                                            scheduleDailyNotification(id.toString(), hour.toInt(), minute.toInt())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun scheduleDailyNotification(timerId: String, hour: Int = 0, minute: Int = 0) {

        cancelScheduledNotification(timerId)
        val currentTime = Calendar.getInstance().timeInMillis
        Log.i(tag, "Scheduled time: $hour : $minute")
        val inputData = Data.Builder()
            .putString("NOTIFICATION_TITLE", "Hydration Reminder")
            .putString("NOTIFICATION_MESSAGE", "It's time to drink water!!")
            .build()

        // Set the target time to 9:00 AM
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        // Calculate delay until 9:00 AM
        val delay = if (targetTime > currentTime) {
            targetTime - currentTime
        } else {
            targetTime + TimeUnit.DAYS.toMillis(1) - currentTime
        }

        // Create a periodic work request with 24-hour interval and initial delay
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        workRequestIds[timerId] = workRequest.id
        // Enqueue the work request
        WorkManager.getInstance(this).enqueue(workRequest)
    }
    private fun cancelScheduledNotification(timerId: String) {
        workRequestIds[timerId]?.let { workRequestId ->
            WorkManager.getInstance(this).cancelWorkById(workRequestId)
            Log.i(tag, "Canceled notification for timer ID: $timerId")
            workRequestIds.remove(timerId) // Remove the entry after cancellation
        } ?: Log.w(tag, "No scheduled notification found for timer ID: $timerId")
    }
}
