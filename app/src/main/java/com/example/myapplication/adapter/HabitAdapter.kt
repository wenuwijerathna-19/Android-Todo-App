package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Habit
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HabitAdapter(val itemList:List<Habit>) : RecyclerView.Adapter<HabitAdapter.HabitAdapterViewHolder>() {

    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_habit,parent,false)
        return HabitAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitAdapterViewHolder, position: Int) {

        val habit = itemList[position]
        holder.nameView.text = habit.name
        holder.timeView.text = convertMillisToAmPmTime(habit.time)

        // set delete
        holder.deleteBtn.setOnClickListener {
            listener?.onDelete(holder.adapterPosition)
        }

        // completion toggle
        holder.completedCheck.setOnCheckedChangeListener(null)
        val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        // ask listener to provide current completion state if available
        val completed = listener?.isCompletedForPosition(holder.adapterPosition, isoDate) ?: false
        holder.completedCheck.isChecked = completed

        holder.completedCheck.setOnCheckedChangeListener { _, isChecked ->
            listener?.onToggleComplete(holder.adapterPosition, isoDate, isChecked)
        }

        // long press to edit
        holder.itemView.setOnLongClickListener {
            listener?.onEdit(holder.adapterPosition)
            true
        }

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class HabitAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val nameView: TextView = itemView.findViewById(R.id.nameView)
    val timeView: TextView = itemView.findViewById(R.id.timeView)
    val deleteBtn: MaterialButton = itemView.findViewById(R.id.deleteBtn)
    val completedCheck: CheckBox = itemView.findViewById(R.id.completedCheck)

    }

    fun convertMillisToAmPmTime(milliseconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    interface Listener{
        fun onDelete(position: Int)
        fun onEdit(position: Int)
        fun onToggleComplete(position: Int, isoDate: String, completed: Boolean)
        // adapter asks host if item at position is completed for provided date
        fun isCompletedForPosition(position: Int, isoDate: String): Boolean
    }

}