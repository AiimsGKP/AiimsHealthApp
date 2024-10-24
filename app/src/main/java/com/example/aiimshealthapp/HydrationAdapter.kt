// TimerAdapter.kt
package com.example.aiimshealthapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.aiimshealthapp.models.Timer


interface OnTimerRemoveListener {
    fun onRemove(timer: Timer)
    fun onUpdate(timer: Timer)
    fun onDisable(timer: Timer)
}
class HydrationAdapter(private val timers: List<Timer>, private val removeListener: OnTimerRemoveListener) : RecyclerView.Adapter<HydrationAdapter.TimerViewHolder>() {
    private val tag = "CHECK_RESPONSE"
    class TimerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val time: TextView = itemView.findViewById(R.id.timeTextView)
        val switch: Switch = itemView.findViewById(R.id.activateBtn)
        val removeButton: Button = itemView.findViewById(R.id.removeBtn)
        val updateTimer: LinearLayout = itemView.findViewById(R.id.layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_hydration_layout, parent, false)
        return TimerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        val timer = timers[position]
        holder.time.text = String.format("%02d:%02d %s", timer.hour.toInt(), timer.minute.toInt(), timer.amPm)
        holder.switch.isChecked = timer.activated

        holder.removeButton.setOnClickListener {
            removeListener.onRemove(timer) // Trigger the callback
            Log.i(tag, "Deleted")
        }

        holder.updateTimer.setOnClickListener {
            removeListener.onUpdate(timer)
        }
        // You can handle the switch toggle event here if needed
        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            timer.activated = isChecked
            removeListener.onDisable(timer)
        }
    }


    override fun getItemCount(): Int = timers.size
}
