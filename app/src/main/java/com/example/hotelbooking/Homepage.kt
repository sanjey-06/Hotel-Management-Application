package com.example.hotelbooking

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import java.util.Calendar

class Homepage : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        // Get user email from intent
        val userEmail = intent.getStringExtra("USER_EMAIL")

        // Calendar button
        val calendarButton: Button = findViewById(R.id.view_calendar_button)

        // Set onClickListener to open the calendar
        calendarButton.setOnClickListener {
            showDateRangePicker { checkIn, checkOut ->
                calendarButton.text = "Check-in: $checkIn\nCheck-out: $checkOut"
            }
        }
    }

    private fun showDateRangePicker(onDatesSelected: (String, String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        var selectedCheckInDate = ""
        var selectedCheckOutDate = ""

        // Show the first DatePickerDialog for Check-In
        DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            selectedCheckInDate = "${dayOfMonth}/${monthOfYear + 1}/$year"

            // Show the second DatePickerDialog for Check-Out
            DatePickerDialog(this, { _, yearOut, monthOfYearOut, dayOfMonthOut ->
                selectedCheckOutDate = "${dayOfMonthOut}/${monthOfYearOut + 1}/$yearOut"
                onDatesSelected(selectedCheckInDate, selectedCheckOutDate)
            }, year, month, day).show()

        }, year, month, day).show()
    }
}
