package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.airbnb.lottie.LottieAnimationView
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.Log
import android.view.View

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SplashActivity", "SplashActivity started")
        setContentView(R.layout.splash_screen)

        // Initialize the Preferences to ensure onboarding flag is set properly
        PreferencesManager.initializeOnboardingFlag(this)

        val lottieAnimationView: LottieAnimationView = findViewById(R.id.animationView)

        // Start the Lottie animation
        lottieAnimationView.playAnimation()

        // Add an animator listener to handle when the animation is done
        lottieAnimationView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)

                // Apply fade-out animation to the Lottie animation
                val fadeOut = AlphaAnimation(1.0f, 0.0f)
                fadeOut.duration = 1000 // 1 second duration
                fadeOut.setAnimationListener(object :
                    android.view.animation.Animation.AnimationListener {
                    override fun onAnimationStart(animation: android.view.animation.Animation) {}

                    override fun onAnimationEnd(animation: android.view.animation.Animation) {
                        // Check onboarding completion status and navigate accordingly
                        navigateToNextScreen()
                    }

                    override fun onAnimationRepeat(animation: android.view.animation.Animation) {}
                })

                lottieAnimationView.startAnimation(fadeOut)
                lottieAnimationView.visibility =
                    View.INVISIBLE // Hide the Lottie animation after fading out
            }
        })
    }

    private fun navigateToNextScreen() {
        Log.d("SplashActivity", "SplashActivity - Checking if onboarding is completed...")

        // Retrieve SharedPreferences to check if onboarding is completed
        val isOnboardingCompleted = PreferencesManager.isOnboardingCompleted(this)

        Log.d("SplashActivity", "Onboarding completion status: $isOnboardingCompleted")

        if (isOnboardingCompleted) {
            // Navigate to Homepage if onboarding is completed
            val intent = Intent(this, Homepage::class.java)
            startActivity(intent)
            Log.d("SplashActivity", "Navigating to Homepage")
        } else {
            // Navigate to OnboardingScreen if onboarding is not completed
            val intent = Intent(this, OnboardingScreen::class.java)
            startActivity(intent)
            Log.d("SplashActivity", "Navigating to OnboardingScreen")
        }
        finish() // Close SplashActivity so it can't be accessed back
    }
}
