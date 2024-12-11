package com.example.hotelbooking

import android.app.Dialog
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class Booknowpage : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var selectedDateTextView: TextView
    private lateinit var selectedOccupantsTextView: TextView


    private var startDate: String? = null
    private var endDate: String? = null
    private var adults: Int = 1
    private var children: Int = 0
    private var rooms: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.booknowpage)

//        val selectedOccupantsTextView = findViewById<TextView>(R.id.selectedOccupants)
        selectedDateTextView = findViewById(R.id.selectedDate)
        selectedOccupantsTextView = findViewById(R.id.selectedOccupants)


        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val occupants: Button = findViewById(R.id.occupants)
        occupants.setOnClickListener {
            showOccupantsDialog()
        }
        val datePickerButton = findViewById<View>(R.id.datePicker)
        datePickerButton.setOnClickListener {
            datePickerDialog()
        }

        val searchbutton: Button = findViewById(R.id.searchbutton)

        searchbutton.setOnClickListener{
            if (validateInputs()) { // This line calls the validateInputs() function
                navigateToSearchingpage()
            }
        }


        val createAccountButton: Button = findViewById(R.id.createAccountButton)
        val loginButton: Button = findViewById(R.id.loginButton)


        // BottomNavigationView setup
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomnavbar)

        // Remove highlighting for the home icon
        bottomNavigationView.menu.findItem(R.id.page_1).isCheckable = false

        // Handle navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_1 -> {
                    val intent = Intent(this, Homepage::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish() // Close current activity
                    true
                }

                R.id.page_2 -> {
                    // Redirect to MyBookings activity
                    val intent = Intent(this, MyBookings::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish() // Close current activity
                    true
                }

                else -> false
            }
        }

        // Check if the user is logged in and confirm its validity
        val currentUser = auth.currentUser
        if (currentUser != null) {
            confirmUserLoggedInAndFetchName(currentUser.uid, loginButton, createAccountButton)
        } else {
            loginButton.text = "Login"
            createAccountButton.visibility = View.VISIBLE
        }

        createAccountButton.setOnClickListener {
            navigateToSignup()
        }


        loginButton.setOnClickListener {
            if (auth.currentUser == null) {
                navigateToLogin()
            } else {
                showPopupMenu(loginButton)
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (startDate.isNullOrEmpty() || endDate.isNullOrEmpty()) {
            Toast.makeText(this, "Please select dates.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (adults + children <= 0 || rooms <= 0) {
            Toast.makeText(this, "Please select valid occupants.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun confirmUserLoggedInAndFetchName(
        userId: String,
        loginButton: Button,
        createAccountButton: Button
    ) {
        database.child("users").child(userId).child("firstName").get()
            .addOnSuccessListener { snapshot ->
                val firstName = snapshot.value as? String
                if (firstName != null) {
                    loginButton.text = firstName
                    createAccountButton.visibility = View.GONE
                    loginButton.setOnClickListener {
                        showPopupMenu(loginButton)
                    }
                } else {
                    auth.signOut()
                    loginButton.text = "Login"
                    createAccountButton.visibility = View.VISIBLE
                    Toast.makeText(
                        this,
                        "Login session invalid. Please log in again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                auth.signOut()
                loginButton.text = "Login"
                createAccountButton.visibility = View.VISIBLE
                Toast.makeText(
                    this,
                    "Unable to verify login. Please log in again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showPopupMenu(view: Button) {
        val popupMenu = PopupMenu(this, view)
        val menuInflater = popupMenu.menuInflater
        menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

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
        auth.signOut()
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, Homepage::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finishAffinity()
    }

    private fun navigateToSignup() {
        val intent = Intent(this, SigninActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun datePickerDialog() {
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setSelection(
                    Pair(
                        MaterialDatePicker.todayInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds() + 3600000 * 24
                    )
                )
                .setTheme(R.style.MaterialCalendarTheme)
                .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection: Pair<Long, Long>? ->
            if (selection != null) {
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

                // Convert selected dates (in milliseconds) to formatted date strings
                startDate = dateFormat.format(Date(selection.first)) // Update class variable
                endDate = dateFormat.format(Date(selection.second))  // Update class variable

                // Update the TextView with selected date range
                selectedDateTextView.text = "$startDate - $endDate"
            }
        }

        dateRangePicker.show(supportFragmentManager, "dateRangePicker")
    }


    private fun navigateToSearchingpage() {
        val intent = Intent(this, Searchingpage::class.java)
        intent.putExtra("startDate", startDate)
        intent.putExtra("endDate", endDate)
        intent.putExtra("adults", adults)
        intent.putExtra("children", children)
        intent.putExtra("rooms", rooms)
        startActivity(intent)
    }

    private fun displaySelectedDates(startDate: Long, endDate: Long) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val start = dateFormat.format(Date(startDate))
        val end = dateFormat.format(Date(endDate))

        selectedDateTextView.text = "$start - $end"
    }

    private fun showOccupantsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_box) // Use dialog_box.xml

        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT // Set the width to match parent
        layoutParams?.height =
            ViewGroup.LayoutParams.WRAP_CONTENT // Set height to wrap content (or any other value you prefer)
        window?.attributes = layoutParams

        dialog.setCanceledOnTouchOutside(false) // Prevent closing the dialog on outside touch

        val occupantsListView = dialog.findViewById<RecyclerView>(R.id.occupants_list_view)
        val closeButton = dialog.findViewById<Button>(R.id.close_button)

        // Sample data for the adapter
        val items = listOf("Adults", "Children", "Rooms") // Item names
        val counters = mutableListOf(adults, children, rooms) // Use the current status

        // Initialize the adapter with context, items, counters, and a callback to update the selectedOccupants TextView
        val adapter = CounterAdapter(this, items, counters) { updatedCounts ->
            // Update the selectedOccupants TextView when counts change
             adults = updatedCounts[0]
             children = updatedCounts[1]
             rooms = updatedCounts[2]

            // Reflect the updated values in the selectedOccupants TextView
            selectedOccupantsTextView.text = "Adults: $adults, Children: $children, Rooms: $rooms"

            // Find the TextView where the selected occupants are displayed (update it)
            val selectedOccupantsTextView = findViewById<TextView>(R.id.selectedOccupants)
            selectedOccupantsTextView.text = "Adults: $adults, Children: $children, Rooms: $rooms"
        }

        occupantsListView.adapter = adapter
        occupantsListView.layoutManager = LinearLayoutManager(this)

        // Handle close button click
        closeButton.setOnClickListener {
            // Update the actual variables when Save is clicked
            adults = counters[0]
            children = counters[1]
            rooms = counters[2]

            // Update the selected occupants TextView
            selectedOccupantsTextView.text = "Adults: $adults, Children: $children, Rooms: $rooms"

            // Dismiss the dialog
            dialog.dismiss()
        }

        dialog.show()
    }
}

