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


interface OnMedTimerRemoveListener {
    fun onRemove(timer: Timer)
    fun onUpdate(timer: Timer)
    fun onDisable(timer: Timer)
}
class MedicationAdapter(
    private val timers: List<Timer>,
    private val removeListener: OnMedTimerRemoveListener
) : RecyclerView.Adapter<MedicationAdapter.TimerViewHolder>() {

    private val tag = "CHECK_RESPONSE"

    class TimerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout: LinearLayout = itemView.findViewById(R.id.layout)
        val title: TextView = layout.findViewById(R.id.medicineTitle)
        val time: TextView = layout.findViewById(R.id.timeTextView)
        val switch: Switch = itemView.findViewById(R.id.activateBtn)
        val removeButton: Button = itemView.findViewById(R.id.removeBtn)
        val updateTimer: LinearLayout = itemView.findViewById(R.id.layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_medication_layout, parent, false)

        return TimerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        val timer = timers[position]
        holder.title.text = timer.title
        holder.time.text = String.format("%02d:%02d %s", timer.hour.toInt(), timer.minute.toInt(), timer.amPm)
        holder.switch.isChecked = timer.activated

        holder.removeButton.setOnClickListener {
            removeListener.onRemove(timer)
            Log.i(tag, "Deleted timer: $timer")
        }

        holder.updateTimer.setOnClickListener {
            removeListener.onUpdate(timer)
        }

        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            timer.activated = isChecked
            removeListener.onDisable(timer)
        }
    }

    override fun getItemCount(): Int = timers.size
}

