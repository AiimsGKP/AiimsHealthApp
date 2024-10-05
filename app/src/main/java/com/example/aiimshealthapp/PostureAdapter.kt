package com.example.aiimshealthapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostureAdapter(
    private val cardList: List<Map<String, String>>,
    private val itemClickListener: (Map<String, String>) -> Unit // Lambda for click handling
) : RecyclerView.Adapter<PostureAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardTitle: TextView = itemView.findViewById(R.id.cardTitle)
        val cardDesc: TextView = itemView.findViewById(R.id.cardDesc)
        val duration: TextView = itemView.findViewById(R.id.duration)
        val cardColor: View = itemView.findViewById(R.id.cardColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.posture_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val cardItem = cardList[position]

        holder.cardTitle.text = cardItem["title"] ?: "No Title"
        holder.cardDesc.text = cardItem["description"] ?: "No Description"
        holder.duration.text = "${cardItem["duration"] ?: "0"} secs"

        // Set background color safely
        val colorString = cardItem["color"]?.trim() ?: "#FFFFFF" // Default to white
        try {
            val color = Color.parseColor(colorString)
            holder.cardColor.backgroundTintList = ColorStateList.valueOf(color)
        } catch (e: IllegalArgumentException) {
            Log.e("PostureAdapter", "Invalid color string: $colorString, using default color.")
            holder.cardColor.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF")) // Fallback to white
        }

        // Set click listener for the item
        holder.itemView.setOnClickListener {
            itemClickListener(cardItem) // Pass the selected card item to the listener
        }
    }

    override fun getItemCount(): Int {
        return cardList.size
    }
}

