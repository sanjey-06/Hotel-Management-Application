package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Homepage : ComponentActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        val createAccountButton: Button = findViewById(R.id.createAccountButton)
        val loginButton: Button = findViewById(R.id.loginButton)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Check if the user is logged in and confirm its validity
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Fetch the user's first name and update UI accordingly
            confirmUserLoggedInAndFetchName(currentUser.uid, loginButton, createAccountButton)
        } else {
            loginButton.text = "Login"
            createAccountButton.visibility = View.VISIBLE  // Show Create Account button when not logged in
        }

        // Create Account button click
        createAccountButton.setOnClickListener {
            navigateToSignup()
        }

        // Login button click
        loginButton.setOnClickListener {
            if (auth.currentUser == null) {
                // User is not logged in, navigate to login page
                navigateToLogin()
            } else {
                // Show the dropdown menu when the first name is clicked
                showPopupMenu(loginButton)
            }
        }
    }

    private fun confirmUserLoggedInAndFetchName(userId: String, loginButton: Button, createAccountButton: Button) {
        database.child("users").child(userId).child("firstName").get()
            .addOnSuccessListener { snapshot ->
                val firstName = snapshot.value as? String
                if (firstName != null) {
                    // Set the login button text to the user's first name
                    loginButton.text = firstName
                    // Hide the Create Account button if the user is logged in
                    createAccountButton.visibility = View.GONE
                    // Set the login button to open the dropdown menu when clicked
                    loginButton.setOnClickListener {
                        showPopupMenu(loginButton)
                    }
                } else {
                    // If first name is not found, sign out the user
                    auth.signOut()
                    loginButton.text = "Login"
                    createAccountButton.visibility = View.VISIBLE // Show Create Account button again
                    Toast.makeText(this, "Login session invalid. Please log in again.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                // Handle failure when fetching data from Firebase
                auth.signOut()
                loginButton.text = "Login"
                createAccountButton.visibility = View.VISIBLE // Show Create Account button again
                Toast.makeText(this, "Unable to verify login. Please log in again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPopupMenu(view: Button) {
        // Create and show a popup menu when the login button is clicked
        val popupMenu = PopupMenu(this, view)
        val menuInflater = popupMenu.menuInflater
        menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        // Set up click listener for the menu items
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_signout -> {
                    signOutAndRedirect()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun signOutAndRedirect() {
        // Sign out the user from Firebase
        auth.signOut()

        // Redirect to homepage and show Toast
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()

        // Clear the activity stack and start the homepage activity as the root
        val intent = Intent(this, Homepage::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finishAffinity() // Close the current activity and all activities in the stack
    }

    private fun navigateToSignup() {
        // Navigate to the Sign In activity
        val intent = Intent(this, SigninActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        // Navigate to the login activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        // Override the back button behavior to exit the app after login
        if (auth.currentUser != null) {
            // If the user is logged in, exit the app directly
            finishAffinity() // Close all activities and quit the app
        } else {
            // If the user is not logged in, go to the previous screen (Homepage)
            super.onBackPressed()
        }
    }
}
