package com.example.aiimshealthapp
import android.os.Bundle


import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.aiimshealthapp.Fragments.BreathingExercisesFragment
import com.example.aiimshealthapp.Fragments.BreathingPageFragment
import com.example.aiimshealthapp.Fragments.DashboardFragment
import com.example.aiimshealthapp.Fragments.ExploreFragment
import com.example.aiimshealthapp.Fragments.FoodScannerFragment
import com.example.aiimshealthapp.Fragments.NotificationFragment
import com.example.aiimshealthapp.Fragments.SettingFragment
import com.example.aiimshealthapp.Fragments.StepTrackerFragment
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.databinding.ActivityDashboardBinding

class Dashboard : AppCompatActivity() {
    lateinit var binding : ActivityDashboardBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        setContentView(binding.root)
        replaceFragment(DashboardFragment())
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Handle home navigation
                    replaceFragment(DashboardFragment())
                }
                R.id.menu_explore -> {
                    // Handle explore navigation
                    replaceFragment(ExploreFragment())
                }
                R.id.menu_notification -> {
                    // Handle notification navigation
                    replaceFragment(NotificationFragment())
                }
                R.id.menu_settings -> {
                    // Handle settings navigation
                    replaceFragment(SettingFragment())
                }
            }
            true
        }


    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

}

