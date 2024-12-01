package com.example.hotelbooking

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class Homepage : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        // Get user email from intent
        val userEmail = intent.getStringExtra("USER_EMAIL")


    }
}
