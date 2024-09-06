package com.example.aiimshealthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aiimshealthapp.R

class TextSliderAdapter(private val texts: List<String>) :
    RecyclerView.Adapter<TextSliderAdapter.TextViewHolder>() {

    inner class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_text_slide, parent, false)
        return TextViewHolder(view)
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        val actualPosition = position % texts.size
        holder.textView.text = texts[actualPosition]
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE // Allows infinite scrolling
    }
}
