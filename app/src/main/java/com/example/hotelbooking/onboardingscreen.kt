package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class OnboardingScreen : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var onboardingAdapter: OnboardingAdapter
    private var onboardingList = listOf(
        OnboardingItem(R.drawable.google, "Welcome to Hotel Booking"),
        OnboardingItem(R.drawable.hotellogo, "Find the best rooms"),
        OnboardingItem(R.drawable.facebook, "Book easily and enjoy your stay")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Set up RecyclerView for horizontal sliding
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initialize the adapter with the list and the onButtonClick callback
        onboardingAdapter = OnboardingAdapter(this, onboardingList) { position ->
            if (position < onboardingList.size - 1) {
                // Scroll to the next page
                recyclerView.smoothScrollToPosition(position + 1)
            } else {
                // Onboarding is completed, navigate to Homepage
                PreferencesManager.setOnboardingCompleted(this, true)
                Toast.makeText(this, "Onboarding completed!", Toast.LENGTH_SHORT).show()

                // Navigate to Homepage after completion
                val intent = Intent(this, Homepage::class.java)
                startActivity(intent)

                finish()  // Close the OnboardingScreen so it can't be accessed back
            }
        }
        recyclerView.adapter = onboardingAdapter
    }
}
