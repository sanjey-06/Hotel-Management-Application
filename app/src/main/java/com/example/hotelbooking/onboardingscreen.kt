package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

class OnboardingScreen : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var onboardingAdapter: OnboardingAdapter
    private var onboardingList = listOf(
        OnboardingItem(R.drawable.hotellogo, "Welcome to Hotel Booking, we assure you an easy and comfy stay!\n"),
        OnboardingItem(R.drawable.oboardingscreen2, "Book a room with a few clicks!\n"),
        OnboardingItem(R.drawable.oboardingscreen3, "Pack your bags and enjoy a stay with us!\n")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Initialize the onboarding flag if it's the first launch
        PreferencesManager.initializeOnboardingFlag(this)

        // Log to verify flag status before checking it
        Log.d("OnboardingScreen", "Is Onboarding Completed? ${PreferencesManager.isOnboardingCompleted(this)}")

        // Check if onboarding is already completed
        if (PreferencesManager.isOnboardingCompleted(this)) {
            Log.d("OnboardingScreen", "Onboarding already completed, navigating to homepage.")
            navigateToHomepage() // If completed, skip onboarding
            return
        }

        // Set up RecyclerView for horizontal sliding
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initialize the adapter with the list and the onButtonClick callback
        onboardingAdapter = OnboardingAdapter(this, onboardingList) { position ->
            if (position < onboardingList.size - 1) {
                // Scroll to the next page
                recyclerView.smoothScrollToPosition(position + 1)
            } else {
                // Onboarding is completed, mark as done and navigate to Homepage
                PreferencesManager.setOnboardingCompleted(this, true)
                Toast.makeText(this, "Onboarding completed!", Toast.LENGTH_SHORT).show()
                navigateToHomepage()
            }
        }
        recyclerView.adapter = onboardingAdapter
    }

    private fun navigateToHomepage() {
        val intent = Intent(this, Homepage::class.java)
        startActivity(intent)
        finish() // Close the OnboardingScreen so it can't be accessed back
    }
}
