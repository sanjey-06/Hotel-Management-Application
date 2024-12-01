package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Load the fade-in animation
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // Get the root layout
        val rootView = findViewById<ViewGroup>(android.R.id.content)

        // Apply the fade-in animation
        rootView.startAnimation(fadeIn)


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Find the input fields, login, and register buttons
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val loginButton: Button = findViewById(R.id.logInButton)
        val registerButton: Button = findViewById(R.id.registerButton)

        // Handle Login button click
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Register button click (navigate to SigninActivity)
        registerButton.setOnClickListener {
            navigateToSignin()
        }
    }

    // Login user with email and password
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful
                    val user = auth.currentUser
                    Log.d(TAG, "signInWithEmail:success")
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                    // Navigate to HomePageActivity (Main App)
                    navigateToHomePage()
                } else {
                    // Login failed
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Navigate to HomePageActivity after successful login
    private fun navigateToHomePage() {
        val intent = Intent(this, Homepage::class.java) // Assuming Homepage is your welcome page
        startActivity(intent)
        finish()  // Close MainActivity so the user can't navigate back
    }

    // Navigate to SigninActivity when Register button is clicked
    private fun navigateToSignin() {
        val intent = Intent(this, SigninActivity::class.java) // Navigate to the Signin page for registration
        startActivity(intent)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
