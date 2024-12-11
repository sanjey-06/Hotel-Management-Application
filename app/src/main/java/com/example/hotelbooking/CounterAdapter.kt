package com.example.hotelbooking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CounterAdapter(
    private val context: Context,
    private val items: List<String>,
    private val counts: MutableList<Int>,
    private val onCountChanged: (List<Int>) -> Unit // Callback to notify the activity when counts change
) : RecyclerView.Adapter<CounterAdapter.CounterViewHolder>() {

    // ViewHolder for the adapter
    class CounterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.item_name)
        val countText: TextView = view.findViewById(R.id.item_count)
        val incrementButton: FloatingActionButton = view.findViewById(R.id.increment_button)
        val decrementButton: FloatingActionButton = view.findViewById(R.id.decrement_button)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounterViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_item, parent, false)
        return CounterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CounterViewHolder, position: Int) {
        holder.itemName.text = items[position]
        holder.countText.text = counts[position].toString()

        holder.incrementButton.setOnClickListener {
            counts[position]++
            holder.countText.text = counts[position].toString()
            onCountChanged(counts) // Notify the activity of the count change
        }

        holder.decrementButton.setOnClickListener {
            // Ensure that "Adults" and "Rooms" cannot go below 1
            if ((items[position] == "Adults" || items[position] == "Rooms") && counts[position] > 1) {
                counts[position]--
                holder.countText.text = counts[position].toString()
                onCountChanged(counts) // Notify the activity of the count change
            } else if (items[position] != "Adults" && items[position] != "Rooms" && counts[position] > 0) {
                counts[position]--
                holder.countText.text = counts[position].toString()
                onCountChanged(counts) // Notify the activity of the count change
            }
        }
    }
    override fun getItemCount(): Int = items.size
}
