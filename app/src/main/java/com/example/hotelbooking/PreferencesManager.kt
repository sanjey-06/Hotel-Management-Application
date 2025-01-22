package com.example.hotelbooking

import android.content.Context
import android.util.Log

object PreferencesManager {
    const val PREFS_NAME = "preferences"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    // This will be called to check if onboarding has been completed
    fun isOnboardingCompleted(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isCompleted = sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        Log.d("PreferencesManager", "Onboarding completed status: $isCompleted") // Log the result
        return isCompleted
    }

    // This will be called to set onboarding as completed
    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.d("PreferencesManager", "Setting onboarding completed: $completed") // Log when the flag is set
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    // This will be called to initialize preferences during the first app launch
    fun initializeOnboardingFlag(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check if the flag already exists
        if (!sharedPreferences.contains(KEY_ONBOARDING_COMPLETED)) {
            // This is the first launch; set the flag to false
            setOnboardingCompleted(context, false)
            Log.d("PreferencesManager", "First launch detected. Onboarding flag initialized to false.")
        } else {
            Log.d("PreferencesManager", "Onboarding flag already set, skipping initialization.")
        }
    }
}
