package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class SigninActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 9001  // Request code for Google Sign-In

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signin)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Find the input fields and buttons
        val firstNameInput: EditText = findViewById(R.id.firstName)
        val lastNameInput: EditText = findViewById(R.id.lastName)
        val mobileNumberInput: EditText = findViewById(R.id.mobilenumber)
        val emailInput: EditText = findViewById(R.id.email_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val confirmPasswordInput: EditText = findViewById(R.id.confirmPassword_input)
        val signUpButton: Button = findViewById(R.id.sign_up_button)
        val googleSignInButton: Button = findViewById(R.id.googlebutton)  // Google Sign-In Button
        val imageView: ImageView = findViewById(R.id.imageView2)
        imageView.setImageResource(R.drawable.top_background1)

        // Set up Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // From Firebase Console
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Handle Sign-Up button click
        signUpButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val mobileNumber = mobileNumberInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // Validate inputs
            if (firstName.isNotEmpty() && lastName.isNotEmpty() && mobileNumber.isNotEmpty() &&
                email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
            ) {
                if (password == confirmPassword) {
                    registerUser(firstName, lastName, mobileNumber, email, password)
                } else {
                    Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Sign-In Button click listener
        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    // Register user with email and password
    private fun registerUser(firstName: String, lastName: String, mobileNumber: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    val user = auth.currentUser
                    Log.d(TAG, "createUserWithEmail:success")
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                    // Store user information in the database
                    storeUserData(user?.uid, firstName, lastName, mobileNumber, email)

                    // Navigate to Homepage after successful registration
                    navigateToHomepage()
                } else {
                    // Registration failed
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Store user data in Firebase Realtime Database
    private fun storeUserData(userId: String?, firstName: String, lastName: String, mobileNumber: String, email: String) {
        if (userId == null) return

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users").child(userId)

        val userData = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "mobileNumber" to mobileNumber,
            "email" to email
        )

        usersRef.setValue(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User data saved successfully.")
                } else {
                    Log.w(TAG, "Failed to save user data.", task.exception)
                }
            }
    }

    // Firebase authentication with Google credentials
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success, update UI
                    val user = auth.currentUser
                    Toast.makeText(this, "Google Sign-In successful!", Toast.LENGTH_SHORT).show()

                    // Store user information in Firebase
                    storeUserData(user?.uid, user?.displayName ?: "", "", "", user?.email ?: "")

                    // Navigate to Homepage after successful sign-in
                    navigateToHomepage()
                } else {
                    // If sign-in fails, display a message to the user
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Navigate to Homepage after registration or sign-in
    private fun navigateToHomepage() {
        val intent = Intent(this, Homepage::class.java)
        startActivity(intent)
        finish()  // Close SigninActivity so the user can't navigate back to the sign-up screen
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

    companion object {
        private const val TAG = "SigninActivity"
    }
}
