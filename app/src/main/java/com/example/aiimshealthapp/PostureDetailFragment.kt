package com.example.aiimshealthapp

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class PostureDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_posture_detail, container, false)

        // Retrieve the data from arguments
        val title = arguments?.getString("title")
        val description = arguments?.getString("description")
        val duration = arguments?.getString("duration")
        val color = arguments?.getString("color")

        // Bind the data to the views
        val titleTextView: TextView = view.findViewById(R.id.detailTitle)
        val descriptionTextView: TextView = view.findViewById(R.id.detailDescription)
        val durationTextView: TextView = view.findViewById(R.id.detailDuration)

        titleTextView.text = title ?: "No Title"
        descriptionTextView.text = description ?: "No Description"
        durationTextView.text = "${duration ?: "0"} secs"

        // Optionally, set the background color if needed
//        view.setBackgroundColor(Color.parseColor(color ?: "#FFFFFF")) // Default to white

        return view
    }
}
