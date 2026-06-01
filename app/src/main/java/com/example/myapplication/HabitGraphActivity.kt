package com.example.myapplication

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.db.HabitPreference
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class HabitGraphActivity : AppCompatActivity() {

    private lateinit var habitPreference: HabitPreference
    private lateinit var backBtn: Button
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_habit_graph)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        habitPreference = HabitPreference(this)
        backBtn = findViewById(R.id.backBtn)
        lineChart = findViewById(R.id.lineChart)

        backBtn.setOnClickListener { finish() }

        setupChart()
    }

    private fun setupChart() {
        val completions = getCompletion()
        val entries = completions.mapIndexed { index, c ->
            Entry(index.toFloat(), c.percentage.toFloat())
        }

        val dataSet = LineDataSet(entries, "Habit Completion %").apply {
            color = ContextCompat.getColor(this@HabitGraphActivity, android.R.color.holo_red_light)
            setCircleColor(ContextCompat.getColor(this@HabitGraphActivity, android.R.color.holo_red_dark))
            circleRadius = 6f
            setDrawCircleHole(true)
            circleHoleColor = Color.WHITE
            lineWidth = 3f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)

            fillDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    ContextCompat.getColor(this@HabitGraphActivity, android.R.color.holo_blue_light),
                    Color.TRANSPARENT
                )
            )
            fillAlpha = 180
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(completions.map { it.date })
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            textSize = 12f
            textColor = Color.DKGRAY
            setDrawGridLines(false)
            labelRotationAngle = -45f
        }

        lineChart.axisLeft.apply {
            axisMaximum = 100f
            axisMinimum = 0f
            granularity = 20f
            textSize = 12f
            textColor = Color.DKGRAY
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}%"
                }
            }
        }
        lineChart.axisRight.isEnabled = false

        lineChart.legend.apply {
            form = Legend.LegendForm.LINE
            textSize = 14f
            textColor = Color.BLACK
            isEnabled = true
        }

        lineChart.description = Description().apply { text = "" }
        
        lineChart.animateX(1200)
        lineChart.animateY(1200)

        lineChart.invalidate()
    }

    private fun getCompletion(): List<Completion> {
        val itemList = habitPreference.getCompletion()
        return itemList.map { Completion(it.key, it.value) }
    }

    data class Completion(val date: String, val percentage: Double)
}