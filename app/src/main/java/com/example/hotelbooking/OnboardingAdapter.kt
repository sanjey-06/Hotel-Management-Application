package com.example.hotelbooking

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class OnboardingAdapter(
    private val context: Context,
    private val onboardingList: List<OnboardingItem>,
    private val onButtonClick: (Int) -> Unit
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val item = onboardingList[position]
        holder.imageView.setImageResource(item.imageRes)
        holder.textView.text = item.text

        // Show "Next" button for all pages except the last one
        holder.nextButton.visibility = if (position == onboardingList.size - 1) {
            View.GONE // Hide the "Next" button on the last page
        } else {
            View.VISIBLE // Show the "Next" button on all other pages
        }

        // Show "Finish" button only on the last page
        holder.finishButton.visibility = if (position == onboardingList.size - 1) {
            View.VISIBLE // Show the "Finish" button on the last page
        } else {
            View.GONE // Hide it on other pages
        }

        holder.nextButton.setOnClickListener {
            onButtonClick(position)
        }

        // When Finish button is clicked, mark onboarding as completed
        holder.finishButton.setOnClickListener {
            // Mark onboarding as completed
            PreferencesManager.setOnboardingCompleted(context, true)

            // Show a Toast confirmation
            Toast.makeText(context, "Onboarding Completed!", Toast.LENGTH_SHORT).show()

            // After setting the flag, navigate to Homepage
            val intent = Intent(context, Homepage::class.java)
            context.startActivity(intent)

            // Finish the onboarding screen to prevent going back to it
            (context as Activity).finish()
        }
    }


    override fun getItemCount(): Int = onboardingList.size

    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.onboardingImageView)
        val textView: TextView = itemView.findViewById(R.id.onboardingTextView)
        val nextButton: MaterialButton = itemView.findViewById(R.id.onboardingNextButton)
        val finishButton: MaterialButton = itemView.findViewById(R.id.onboardingFinishButton) // New Finish button
    }
}
