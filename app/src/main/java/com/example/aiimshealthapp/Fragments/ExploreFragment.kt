package com.example.aiimshealthapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aiimshealthapp.HealthFacitlityLocationActivtiy
import com.example.aiimshealthapp.HealthInfoActivity
import com.example.aiimshealthapp.HydrationReminderActivity
import com.example.aiimshealthapp.MedicationReminderActivity
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.SampleActivity
import com.example.aiimshealthapp.ScreenTimeMonitorActivity
import com.example.aiimshealthapp.StepCounterActivity
import com.example.aiimshealthapp.databinding.FragmentExploreBinding

class ExploreFragment : Fragment() {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.StepTracker.setOnClickListener {
            val intent = Intent(requireContext(), StepCounterActivity::class.java)
            startActivity(intent)
        }
        binding.PhysicalExercises.setOnClickListener {
//            val intent = Intent(requireContext(), SampleActivity::class.java)
//            startActivity(intent)
        }
        binding.Yoga.setOnClickListener {

        }
        binding.HydrationReminder.setOnClickListener {
            val intent = Intent(requireContext(), HydrationReminderActivity::class.java)
            startActivity(intent)
        }
        binding.BMICalculator.setOnClickListener {
//            val intent = Intent(requireContext(), ScreenTimeMonitorActivity::class.java)
//            startActivity(intent)
        }
        binding.PostureExercises.setOnClickListener {
            val secondFragment = PostureExerciseFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                .addToBackStack(null) // Optional: Adds the transaction to the back stack so the user can navigate back
                .commit()
        }
        binding.RelaxingSounds.setOnClickListener {
            val secondFragment = RelaxingSoundsFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                .addToBackStack(null) // Optional: Adds the transaction to the back stack so the user can navigate back
                .commit()
        }
        binding.Meditation.setOnClickListener {

        }
        binding.BreathingExercises.setOnClickListener {
            val secondFragment = BreathingExercisesFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                .addToBackStack(null) // Optional: Adds the transaction to the back stack so the user can navigate back
                .commit()
        }
        binding.CommunicationReminder.setOnClickListener {

        }
        binding.FoPLLabel.setOnClickListener {
            val secondFragment = FoodScannerFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                .addToBackStack(null) // Optional: Adds the transaction to the back stack so the user can navigate back
                .commit()
        }
        binding.HealthEducation.setOnClickListener {
            val intent = Intent(requireContext(), HealthInfoActivity::class.java)
            startActivity(intent)
        }
        binding.MedicationReminder.setOnClickListener {
            val intent = Intent(requireContext(), MedicationReminderActivity::class.java)
            startActivity(intent)
        }
        binding.HealthCheckupReminder.setOnClickListener {

        }
        binding.HealthFacilityLocation.setOnClickListener {
            val intent = Intent(requireContext(), HealthFacitlityLocationActivtiy::class.java)
            startActivity(intent)

        }
        binding.ScreenTimeMonitor.setOnClickListener {
            val intent = Intent(requireContext(), ScreenTimeMonitorActivity::class.java)
            startActivity(intent)
        }


    }
}