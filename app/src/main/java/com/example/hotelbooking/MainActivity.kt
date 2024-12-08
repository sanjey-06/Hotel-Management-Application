package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001 // Request code for Google Sign-In

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
        val googleSignInButton: Button = findViewById(R.id.googlebutton1)  // Google Sign-In Button
        val imageView: ImageView = findViewById(R.id.imageView)

        imageView.setImageResource(R.drawable.top_background1)

        // Set up Google Sign-In with explicit sign-in mode
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Your client ID from Firebase Console
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Handle Google Sign-In button click
        googleSignInButton.setOnClickListener {
            googleSignInClient.signOut() // Sign out any existing account before asking the user to choose an account
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

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

    // Firebase authentication with Google credentials
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Check if the user exists in Firebase Authentication
                    val user = auth.currentUser
                    if (user != null) {
                        // Check if the user exists in the database (users node)
                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid)
                        userRef.get().addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful && dbTask.result.exists()) {
                                // User exists in the database, navigate to homepage
                                Log.d(TAG, "signInWithCredential:success")
                                Toast.makeText(this, "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
                                navigateToHomePage()
                            } else {
                                // User does not exist in the database, show error message
                                Log.w(TAG, "signInWithCredential:failure", task.exception)
                                Toast.makeText(this, "Google account not registered in the app.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // If user does not exist, show an error
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If sign-in fails, display a message to the user
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Handle the result of the Google Sign-In process
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign-In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign-In failed, update UI accordingly
                Log.w(TAG, "signInResult:failed code=" + e.statusCode)
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Navigate to HomePageActivity after successful login or Google sign-in
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
