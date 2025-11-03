package com.example.eventtracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.eventtracker.databinding.ActivityMainBinding
import com.example.eventtracker.util.gone
import com.example.eventtracker.util.visible

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup bottom navigation with nav controller
        binding.bottomNav.setupWithNavController(navController)

        // Hide/show bottom nav based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment,
                R.id.signUpFragment,
                R.id.eventDetailFragment,
                R.id.createEventFragment,
                R.id.myEventsFragment -> binding.bottomNav.gone()
                else -> binding.bottomNav.visible()
            }
        }
    }
}