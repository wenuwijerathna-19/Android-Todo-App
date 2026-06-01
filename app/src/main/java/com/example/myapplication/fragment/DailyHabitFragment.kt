package com.example.myapplication.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.HabitGraphActivity
import com.example.myapplication.R
import com.example.myapplication.adapter.HabitAdapter
import com.example.myapplication.db.HabitPreference
import com.example.myapplication.model.Habit
import com.example.myapplication.utils.WidgetUpdateUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlin.collections.addAll
import kotlin.collections.get
import kotlin.text.clear

class DailyHabitFragment : Fragment(), HabitAddBottomSheetFragment.Listener, HabitAdapter.Listener {

    private lateinit var habitPreference: HabitPreference
    val habitList: MutableList<Habit> = mutableListOf()
    val habitAdapter = HabitAdapter(habitList)
    init {
        habitAdapter.listener = this@DailyHabitFragment
    }

    private lateinit var habitsView: RecyclerView
    private lateinit var addNew: FloatingActionButton
    private lateinit var todaySummaryText: android.widget.TextView
    private lateinit var todaySummaryProgress: LinearProgressIndicator
    private lateinit var getAnalyticsBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        habitPreference = HabitPreference(requireContext())

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        habitsView = view.findViewById(R.id.habitsView)
        todaySummaryText = view.findViewById(R.id.todaySummaryText)
        todaySummaryProgress = view.findViewById(R.id.todaySummaryProgress)
        addNew = view.findViewById(R.id.addNew)
        getAnalyticsBtn = view.findViewById(R.id.getAnalyticsBtn)

        habitsView.setHasFixedSize(true)
        habitsView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
        habitsView.adapter = habitAdapter

        addNew.setOnClickListener {
            HabitAddBottomSheetFragment.show(parentFragmentManager,this)
        }

        getAnalyticsBtn.setOnClickListener {
            startActivity(Intent(requireContext(), HabitGraphActivity::class.java))
        }

        loadHabits()

    }

    @SuppressLint("SetTextI18n")
    fun loadHabits(){

        //clear loaded items
        if(habitList.isNotEmpty()){
            val count =  habitList.size
            habitList.clear()
            habitAdapter.notifyItemRangeRemoved(0,count)
        }

        val list = habitPreference.getAllHabits()
        habitList.addAll(0,list)
        habitAdapter.notifyItemRangeInserted(0,habitList.size)

        // update today's completion summary
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val today = sdf.format(java.util.Date())
        val total = habitList.size
        var completedCount = 0
        for(i in 0 until habitList.size){
            if(habitPreference.isCompleted(habitList[i].time, today)) completedCount++
        }
        val percent = if(total == 0) 0 else (completedCount * 100) / total
        todaySummaryText.text = "Today's completion: ${percent}%"
        todaySummaryProgress.progress = percent

    }

    override fun onAdded() {
        loadHabits()
        // Update widget to reflect new habit addition
        WidgetUpdateUtil.updateAllWidgets(requireContext())
    }

    override fun onDelete(position: Int) {
        MaterialAlertDialogBuilder(requireActivity())
            .setCancelable(false)
            .setMessage("Are you sure to delete this time slot?")
            .setNegativeButton("Cancel"){ p0, p1 ->
                p0.cancel()
            }
            .setPositiveButton("Delete") { p0, p1 ->
                habitPreference.delete(habitList[position].time)
                p0.dismiss()
                habitList.removeAt(position)
                habitAdapter.notifyItemRemoved(position)
                // Update widget to reflect habit deletion
                WidgetUpdateUtil.updateAllWidgets(requireContext())
            }
            .show()
    }

    override fun onEdit(position: Int) {
        val habit = habitList[position]
        // compute hour and minute from habit.time
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = habit.time
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = cal.get(java.util.Calendar.MINUTE)
        HabitAddBottomSheetFragment.showForEdit(parentFragmentManager, this, habit.time, habit.name, hour, minute)
    }

    override fun onToggleComplete(position: Int, isoDate: String, completed: Boolean) {
        val habit = habitList[position]
        habitPreference.setCompletion(habit.time, isoDate, completed)
        // reload to update UI and summary
        loadHabits()
        // Update widget to reflect new completion percentage
        WidgetUpdateUtil.updateAllWidgets(requireContext())
    }

    override fun isCompletedForPosition(position: Int, isoDate: String): Boolean {
        val habit = habitList[position]
        return habitPreference.isCompleted(habit.time, isoDate)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_daily_habit, container, false)
    }

    companion object {
        fun newInstance() = DailyHabitFragment()
    }

}