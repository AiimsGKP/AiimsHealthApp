package com.example.aiimshealthapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.databinding.FragmentBreathingExercisesBinding
import com.example.aiimshealthapp.databinding.FragmentBreathingPageBinding
import com.example.aiimshealthapp.databinding.FragmentStepTrackerBinding


class BreathingExercisesFragment : Fragment() {
    private var _binding: FragmentBreathingExercisesBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBreathingExercisesBinding.inflate(inflater, container, false)

        val boxBreathingData = mapOf(
            "inhale" to "4",
            "hold" to "4",
            "exhale" to "4",
            "hold2" to "4",
            "cycles" to "2",
            "title" to "Box Breathing"
        )
        val breathing478Data = mapOf(
            "inhale" to "4",
            "hold" to "7",
            "exhale" to "8",
            "hold2" to "0",
            "cycles" to "2",
            "title" to "4-7-8 Breathing"
        )

        binding.boxBreathing.setOnClickListener {
            val bundle = Bundle().apply {
                for ((key, value) in boxBreathingData) {
                    putString(key, value)
                }
            }
            val destinationFragment = BreathingPageFragment().apply {
                arguments = bundle
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, destinationFragment)
                .addToBackStack(null)
                .commit()
        }
        binding.breathing478.setOnClickListener {
            val bundle = Bundle().apply {
                for ((key, value) in breathing478Data) {
                    putString(key, value)
                }
            }
            val destinationFragment = BreathingPageFragment().apply {
                arguments = bundle
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, destinationFragment)
                .addToBackStack(null)
                .commit()
        }


        return binding.root
    }
}