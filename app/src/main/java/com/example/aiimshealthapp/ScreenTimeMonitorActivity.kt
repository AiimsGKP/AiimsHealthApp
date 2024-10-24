package com.example.aiimshealthapp

import android.animation.ObjectAnimator
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ScreenTimeMonitorActivity : AppCompatActivity() {
    private lateinit var usageRecyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var loader: LinearLayout
    private val tag = "CHECK_RESPONSE"

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_time_monitor)

        usageRecyclerView = findViewById(R.id.usage_recycler_view)
        emptyView = findViewById(R.id.empty_view)
        loader = findViewById(R.id.loader)

        usageRecyclerView.layoutManager = LinearLayoutManager(this)

        val cachedAppUsageList: List<AppUsage>? = null
//        getAppUsageListFromPrefs()

        if (cachedAppUsageList != null && cachedAppUsageList.isNotEmpty()) {
            Log.i("CHECK_RESPONSE", "Using Cached Data")
            loader.visibility = View.GONE
            usageRecyclerView.adapter = AppUsageAdapter(cachedAppUsageList)
        } else {
            // Check for permission
            if (!hasUsageStatsPermission()) {
                requestUsageStatsPermission()
            } else {
                Log.i("CHECK_RESPONSE", "Permission Granted - Fetching Data")
                // Fetch the app usage statistics asynchronously
                loader.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    val appUsageList = getAppUsageList()

                    withContext(Dispatchers.Main) {
                        loader.visibility = View.GONE
                        if (appUsageList.isEmpty()) {
                            emptyView.visibility = View.VISIBLE
                            usageRecyclerView.visibility = View.GONE
                        } else {
                            emptyView.visibility = View.GONE
                            usageRecyclerView.visibility = View.VISIBLE
                            usageRecyclerView.adapter = AppUsageAdapter(appUsageList)
                            // Save the fetched data for future use
                            saveAppUsageList(appUsageList)
                        }
                    }
                }
            }
        }

        findViewById<View>(R.id.backBtn).setOnClickListener {
            onBackPressed()
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()

        if (hasUsageStatsPermission()) {
            Log.i("CHECK_RESPONSE", "Permission Granted")
            // Hide the loader and fetch data
            emptyView.visibility = View.GONE
            loader.visibility = View.VISIBLE

            // Fetch the app usage statistics asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                val appUsageList = getAppUsageList()

                withContext(Dispatchers.Main) {
                    loader.visibility = View.GONE
                    if (appUsageList.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        usageRecyclerView.visibility = View.GONE
                    } else {
                        emptyView.visibility = View.GONE
                        usageRecyclerView.visibility = View.VISIBLE
                        usageRecyclerView.adapter = AppUsageAdapter(appUsageList)
                        // Save the updated data for future use
                        saveAppUsageList(appUsageList)
                    }
                }
            }
        } else {
            // Show the empty state while permission is not granted
            Log.i("CHECK_RESPONSE", "Permission Not Granted")
            emptyView.visibility = View.VISIBLE
            loader.visibility = View.GONE
            usageRecyclerView.visibility = View.GONE
        }
    }

    private fun saveAppUsageList(appUsageList: List<AppUsage>) {
        val sharedPreferences = getSharedPreferences("app_usage_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(appUsageList)
        editor.putString("app_usage_data", json)
        editor.apply()
    }

    private fun getAppUsageListFromPrefs(): List<AppUsage>? {
        val sharedPreferences = getSharedPreferences("app_usage_prefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("app_usage_data", null)
        return if (json != null) {
            val type = object : TypeToken<List<AppUsage>>() {}.type
            Gson().fromJson(json, type)
        } else {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getAppUsageList(): List<AppUsage> {
        val appUsageList = mutableListOf<AppUsage>()
        val packageManager = packageManager
        val uniqueApps = mutableSetOf<String>()

        // Check if permission is granted
        if (hasUsageStatsPermission()) {
            try {
                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startTime = calendar.timeInMillis

                val currentTime = System.currentTimeMillis()

                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    currentTime
                )
                Log.i(tag, "Start time: $startTime, Current time: $currentTime timezone : ${calendar.timeZone}")


                usageStats?.forEach { usageStat ->
                    if (usageStat.firstTimeStamp >= startTime && usageStat.lastTimeStamp <= currentTime) {
                        // Process this app usage only if it's within today's time range
                        try {
                            val appInfo = packageManager.getApplicationInfo(usageStat.packageName, PackageManager.GET_META_DATA)
                            val appName = packageManager.getApplicationLabel(appInfo).toString()
                            val minutesUsed = TimeUnit.MILLISECONDS.toMinutes(usageStat.totalTimeInForeground)

                            // Only add apps with non-zero usage time
                            if (minutesUsed > 0 && uniqueApps.add(usageStat.packageName)) {
                                appUsageList.add(AppUsage(usageStat.packageName, appName, minutesUsed))
                            }
                        } catch (e: PackageManager.NameNotFoundException) {
                            Log.e("AppUsageActivity", "Package name not found: ${usageStat.packageName}", e)
                        }
                    }

                }

            } catch (e: Exception) {
                Log.e("AppUsageActivity", "Error getting usage stats", e)
            }
        }

        // Sort the list by minutesUsed in descending order
        appUsageList.sortByDescending { it.minutesUsed }

        return appUsageList
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

data class AppUsage(val packageName: String, val appName: String, val minutesUsed: Long)


class AppUsageAdapter(private val appUsageList: List<AppUsage>) : RecyclerView.Adapter<AppUsageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_usage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appUsage = appUsageList[position]
        val packageManager = holder.itemView.context.packageManager

        holder.appNameTextView.text = appUsage.appName
        holder.appProgressBar.max = 180
//        holder.appProgressBar.progress = minOf(appUsage.minutesUsed.toInt(), 180)
        val progressAnimator = ObjectAnimator.ofInt(holder.appProgressBar, "progress", 0, minOf(appUsage.minutesUsed.toInt(), 180))
        progressAnimator.duration = 1000 // Set duration in milliseconds (2 seconds)
        progressAnimator.start()
        val progress = if ((appUsage.minutesUsed.toDouble() / 180) * 100 > 100) "100" else String.format("%.1f", (appUsage.minutesUsed.toDouble() / 180) * 100)
        holder.appProgressTextView.text = "$progress%"
        holder.minutesUsedTextView.text = "${appUsage.minutesUsed / 60} h ${appUsage.minutesUsed % 60} m"

        try {
            val appIcon = packageManager.getApplicationIcon(appUsage.packageName)
            holder.appIconImageView.setImageDrawable(appIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("AppUsageAdapter", "Package name not found: ${appUsage.packageName}", e)
            holder.appIconImageView.setImageResource(R.drawable.icon) // Set a default icon if not found
        }
    }


    override fun getItemCount(): Int = appUsageList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIconImageView: ImageView = view.findViewById(R.id.app_icon_image_view)
        val appNameTextView : TextView = view.findViewById(R.id.appName)
        val appProgressBar : LinearProgressIndicator = view.findViewById(R.id.appProgress)
        val minutesUsedTextView: TextView = view.findViewById(R.id.minutes_used_text_view)
        val appProgressTextView: TextView = view.findViewById(R.id.app_progress_text_view)
    }
}