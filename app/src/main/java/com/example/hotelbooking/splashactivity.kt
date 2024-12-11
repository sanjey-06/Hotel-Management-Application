package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AlphaAnimation
import androidx.activity.ComponentActivity
import com.airbnb.lottie.LottieAnimationView
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

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
                fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                    override fun onAnimationStart(animation: android.view.animation.Animation) {}

                    override fun onAnimationEnd(animation: android.view.animation.Animation) {
                        // Transition to the homepage after the fade-out
                        navigateToHomepage()
                    }

                    override fun onAnimationRepeat(animation: android.view.animation.Animation) {}
                })

                lottieAnimationView.startAnimation(fadeOut)
                lottieAnimationView.visibility = View.INVISIBLE // Hide the Lottie animation after fading out
            }
        })
    }

    private fun navigateToHomepage() {
        val intent = Intent(this, Homepage::class.java)
        startActivity(intent)
        finish() // Close SplashActivity so it can't be accessed back
    }
}
