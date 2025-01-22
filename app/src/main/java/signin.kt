package com.example.hotelbooking

import android.content.Intent
import android.util.Base64
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.facebook.*
import com.facebook.internal.Utility
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SigninActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private val RC_SIGN_IN = 9001  // Request code for Google Sign-In

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signin)

        // Set the Facebook app client token
        FacebookSdk.setClientToken("Enter Your Facebook Client Token")

        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)
        Log.d("Facebook", "Facebook SDK Initialized")

        // Optional: Enable logging behavior for debugging purposes (can be removed later)
        FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS)


        // Log the Facebook Key Hash
        logKeyHash()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        Log.d("Facebook", "Firebase Auth initialized")

        // Initialize Facebook Callback Manager
        callbackManager = CallbackManager.Factory.create()
        Log.d("Facebook", "Facebook Callback Manager initialized")

        // Find the input fields and buttons
        val firstNameInput: EditText = findViewById(R.id.firstName)
        val lastNameInput: EditText = findViewById(R.id.lastName)
        val mobileNumberInput: EditText = findViewById(R.id.mobilenumber)
        val emailInput: EditText = findViewById(R.id.email_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val confirmPasswordInput: EditText = findViewById(R.id.confirmPassword_input)
        val signUpButton: Button = findViewById(R.id.sign_up_button)
        val googleSignInButton: Button = findViewById(R.id.googlebutton)
        val facebookSignInButton: Button = findViewById(R.id.facebookbutton)
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
            GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }

        // Facebook Sign-In Button click listener
        facebookSignInButton.setOnClickListener {
            Log.d("Facebook", "Facebook Sign-In button clicked")
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        Log.d("Facebook1", "Facebook Login success: ${loginResult.accessToken.token}")
                        handleFacebookAccessToken(loginResult.accessToken)
                    }

                    override fun onCancel() {
                        Log.d("Facebook", "Facebook Login canceled")

                        Toast.makeText(this@SigninActivity, "Facebook Login canceled.", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(error: FacebookException) {
                        Log.e("Facebook", "Facebook Login error: ${error.message}")

                        Toast.makeText(this@SigninActivity, "Facebook Login failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }


    private fun logKeyHash() {
        try {
            val info: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)

            // Ensure signatures is not null
            info.signatures?.let { signatures ->
                for (signature in signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                    Log.d("Facebook2", "Key Hash: $keyHash")
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("Facebook2", "Package not found", e)
        } catch (e: NoSuchAlgorithmException) {
            Log.e("Facebook2", "SHA algorithm not found", e)
        }
    }



    // Register user with email and password
    private fun registerUser(firstName: String, lastName: String, mobileNumber: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    storeUserData(user?.uid, firstName, lastName, mobileNumber, email)
                    navigateToHomepage()
                } else {
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
    }

    // Firebase authentication with Google credentials
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Toast.makeText(this, "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
                storeUserData(user?.uid, user?.displayName ?: "", "", "", user?.email ?: "")
                navigateToHomepage()
            } else {
                Toast.makeText(this, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Firebase authentication with Facebook credentials
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("Facebook", "Handling Facebook AccessToken: ${token.token}")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.d("Facebook", "Facebook Sign-In successful for user: ${user?.displayName}")

                Toast.makeText(this, "Facebook Sign-In successful!", Toast.LENGTH_SHORT).show()
                storeUserData(user?.uid, user?.displayName ?: "", "", "", user?.email ?: "")
                navigateToHomepage()
            } else {
                Log.e("Facebook", "Facebook Sign-In failed: ${task.exception?.message}")

                Toast.makeText(this, "Facebook Sign-In failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Navigate to Homepage after registration or sign-in
    private fun navigateToHomepage() {
        val intent = Intent(this, Homepage::class.java)
        startActivity(intent)
        finish()
    }

    // Handle the result of Google and Facebook Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val TAG = "SigninActivity"
    }
}
