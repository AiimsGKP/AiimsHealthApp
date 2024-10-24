package com.example.aiimshealthapp

import android.app.usage.UsageStats
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aiimshealthapp.models.AppUsageStats

class UsageStatsAdapter(private val usageStatsList: List<AppUsageStats>) :
    RecyclerView.Adapter<UsageStatsAdapter.UsageStatsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsageStatsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usage_stats, parent, false)
        return UsageStatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsageStatsViewHolder, position: Int) {
        val appUsageStats = usageStatsList[position]
        holder.bind(appUsageStats)
    }

    override fun getItemCount(): Int {
        return usageStatsList.size
    }

    class UsageStatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appNameTextView: TextView = itemView.findViewById(R.id.app_name)
        private val usageTimeTextView: TextView = itemView.findViewById(R.id.usage_time)

        fun bind(appUsageStats: AppUsageStats) {
            appNameTextView.text = appUsageStats.appName
            usageTimeTextView.text = "${appUsageStats.usageTimeInMinutes} minutes"
        }
    }
}
