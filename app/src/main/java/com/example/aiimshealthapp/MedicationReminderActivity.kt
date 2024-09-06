package com.example.aiimshealthapp

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class MedicationReminderActivity : AppCompatActivity() {
    private var viewIds = mutableListOf<Int>()
    private lateinit var mainLayout: RelativeLayout
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private var timers = mutableListOf<Timer>()
    private var maxCardViewId = 0
    private val tag = "CHECK_RESPONSE"
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_medication_reminder)
        mainLayout = findViewById(R.id.main_layout)
        mainLayout.visibility = View.VISIBLE

        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val medicationReminderSwitch = findViewById<SwitchCompat>(R.id.medicationReminder)

        medicationReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            mainLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        val addTimerBtn = findViewById<Button>(R.id.addTimer)
        addTimerBtn.setOnClickListener {
            generateUniqueId { count ->
                val uniqueId = count + 1
                addTimer(uniqueId)
            }
            medicationReminderSwitch.isChecked = true
        }

        maxCardViewId = getMaxCardViewIdFromDatabase()

        updatePage()

    }

    private fun scheduleActivity() {
        if (currentUser != null) {
            val username = currentUser.email.toString().substringBefore("@")
            val user = User(username ?: "Unknown User", currentUser.email ?: "No Email")
            val userId = auth.currentUser?.uid ?: "unknown_user"
            db.collection("medication")
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
                                        if (hour != null) {
                                            hour = (hour.toInt() + 12).toString()
                                        }
                                    }
                                    if(activated){
                                        val calendar = Calendar.getInstance().apply {
                                            if (hour != null) {
                                                set(Calendar.HOUR_OF_DAY, hour.toInt())
                                            }  // Set the hour for the notification
                                            if (minute != null) {
                                                set(Calendar.MINUTE, minute.toInt())
                                            }        // Set the minute for the notification
                                            set(Calendar.SECOND, 0)        // Set the second for the notification
                                        }
                                        val timeInMillis = calendar.timeInMillis
                                        scheduleNotification(this, id.toString(), timeInMillis, title.toString())
                                    }
                                }
                            }
                        }
                    }}}
    }

    private fun updateDatabase() {
        if (currentUser != null) {
            val username = currentUser.email.toString().substringBefore("@")
            val user = User(username ?: "Unknown User", currentUser.email ?: "No Email")
            val userId = auth.currentUser?.uid ?: "unknown_user"
            val medication = Medication(user, timers)
            db.collection("medication")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        db.collection("medication").document(userId).set(medication)
                            .addOnSuccessListener {
                                updatePage()
                            }
                    } else {
                        val document = querySnapshot.documents.first()
                        val documentId = document.id
                        val newData = medication.toMap()
                        db.collection("medication").document(documentId)
                            .set(newData)
                            .addOnSuccessListener {
                                updatePage()
                            }
                    }
                }
        }
    }

    private fun updatePage() {
        setLoading(true, progressBar)
        mainLayout.removeAllViews()
        timers = mutableListOf<Timer>()
        viewIds = mutableListOf<Int>()
        if(currentUser!= null) {
            db.collection("medication")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot!= null) {
                        for (document in querySnapshot) {
                            val data = document.data
                            val timerss = data["timers"] as? List<Map<String, Any>>
                            if (timerss != null) {
                                for(timer in timerss){
                                    val title = timer["title"] as? String
                                    val activated = (timer["activated"] as? Boolean) ?: true
                                    val id = timer["cardViewId"] as? String
                                    val hour = timer["hour"] as? String
                                    val minute = timer["minute"] as? String
                                    val amPm = timer["amPm"] as? String

                                    timers.add(Timer(title.toString(), activated, id.toString(), hour.toString(), minute.toString(), amPm.toString()))
                                    generateTimeLayout(title.toString(), activated, id.toString().toInt(), hour.toString().toInt(), minute.toString().toInt(), amPm.toString())
                                }
                                setLoading(false, progressBar)
                            }

                        }
                    }
                }
        }
        scheduleActivity()
    }

    private fun generateTimeLayout(title:String, activated: Boolean, cardViewId: Int, hour: Int, minute: Int, amPm: String) {

        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.card_medication_layout, mainLayout, false) as CardView

        cardView.id = cardViewId
        viewIds.add(cardViewId)

        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        if (viewIds.size > 1) {
            params.topMargin = 16
            params.addRule(RelativeLayout.BELOW, viewIds[viewIds.size - 2])
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        }

        cardView.layoutParams = params
        val titleView = cardView.findViewById<TextView>(R.id.medicineTitle)
        titleView.text = title
        val timeTextView = cardView.findViewById<TextView>(R.id.timeTextView)
        timeTextView.text = String.format("%02d:%02d %s", hour, minute, amPm)

        val removeBtn = cardView.findViewById<Button>(R.id.removeBtn)
        val activateBtn = cardView.findViewById<SwitchCompat>(R.id.activateBtn)
        activateBtn.isChecked = activated
        mainLayout.addView(cardView)

        cardView.setOnClickListener {
            updateTimer(cardViewId)
        }

        removeBtn.setOnClickListener {
            removeTimer(cardViewId)
        }
        activateBtn.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                for(timer in timers){
                    if(timer.cardViewId == cardViewId.toString()){
                        timer.activated = true
                        break
                    }
                }
            }
            else{
                for(timer in timers){
                    if(timer.cardViewId == cardViewId.toString()){
                        timer.activated = false
                        break
                    }
                }
            }
            updateDatabase()

        }
    }

    private fun addTimer(cardViewId:Int){
        val inflater = this.layoutInflater // Get the LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_time_picker, null) // Inflate the layout into a View

        val editTextTime = dialogView.findViewById<EditText>(R.id.editTextTime)

        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)

        timePicker.setIs24HourView(false)
        editTextTime?.requestFocus()
        val timePickerDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Set") { dialog, _ ->
                val hourOfDay = if (timePicker.hour == 0) 12 else timePicker.hour % 12
                val minute = timePicker.minute
                val amPm = if (timePicker.hour >= 12) "PM" else "AM"

                val timer = Timer(editTextTime.text.toString(),true, cardViewId.toString(),hourOfDay.toString(), minute.toString(), amPm)
                timers.add(timer)
                updateDatabase()

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        timePickerDialog.show()
    }

    private fun updateTimer(cardViewId: Int) {

        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_time_picker, null)

        val editTextTime = dialogView.findViewById<EditText>(R.id.editTextTime)

        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)

        timePicker.setIs24HourView(false)
        for(timer in timers){
            if(timer.cardViewId == cardViewId.toString()) {
                editTextTime?.setText(timer.title)
                break
            }
        }
        val timePickerDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Set") { dialog, _ ->
                val hourOfDay = if (timePicker.hour == 0) 12 else timePicker.hour % 12
                val minute = timePicker.minute
                val amPm = if (timePicker.hour >= 12) "PM" else "AM"

                for(timer in timers){
                    if(timer.cardViewId == cardViewId.toString()){
                        timer.title = editTextTime.text.toString()
                        timer.amPm = amPm
                        timer.hour = hourOfDay.toString()
                        timer.minute = minute.toString()
                        break
                    }
                }
                updateDatabase()

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        timePickerDialog.show()
    }

    private fun removeTimer(cardViewId: Int) {

        val timerToRemove = timers.find { it.cardViewId == cardViewId.toString() }
        if (timerToRemove != null) {
            timers.remove(timerToRemove)
            viewIds.remove(cardViewId)


            updateDatabase()
        }
    }

    private fun getMaxCardViewIdFromDatabase(): Int {
        var maxId = 0
        if (currentUser != null) {
            db.collection("medication")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot != null) {
                        for (document in querySnapshot) {
                            val data = document.data
                            val timerss = data["timers"] as? List<Map<String, Any>>
                            if (timerss != null) {
                                for (timer in timerss) {
                                    val id = timer["cardViewId"]?.toString()?.toIntOrNull() ?: 0
                                    if (id > maxId) {
                                        maxId = id
                                    }
                                }
                            }
                        }
                    }
                }
        }
        return maxId
    }


    private fun generateUniqueId(callback: (Int) -> Unit) {
        maxCardViewId += 1
        callback(maxCardViewId)
    }

    fun scheduleNotification(context: Context, id:String, timeInMillis: Long, title:String) {

        val currentTimeInMillis = System.currentTimeMillis()
        Log.i(tag, timeInMillis.toString())
        Log.i(tag, currentTimeInMillis.toString())
        // Check if the timeInMillis is in the future
        if (timeInMillis <= currentTimeInMillis) {
            Log.e("CHECK_RESPONSE", "Skipping scheduling as the time is in the past")
            return
        }
        // Create an intent to trigger the NotificationReceiver
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notificationId", id)
            putExtra("notificationTitle", "Medication Reminder")
            putExtra("notificationText", title)
        }

        // Create a PendingIntent to be triggered by the AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get the AlarmManager service
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check for permission to schedule exact alarms if running on Android S or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e("CHECK_RESPONSE", "Cannot schedule exact alarms, requesting permission")
            val settingsIntent = Intent().apply {
                action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(settingsIntent)
            return
        }

        // Schedule the alarm to trigger the notification
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
            Log.e("CHECK_RESPONSE", "Alarm scheduled")
        } catch (e: SecurityException) {
            Log.e("CHECK_RESPONSE", "SecurityException: ${e.message}")
        }
    }
    fun setLoading(loading: Boolean, progressBar: ProgressBar) {
        if (loading) {
            progressBar.visibility = View.VISIBLE // Show the ProgressBar
        } else {
            progressBar.visibility = View.GONE // Hide the ProgressBar
        }
    }

}