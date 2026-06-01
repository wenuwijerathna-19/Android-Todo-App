package com.example.myapplication.fragment

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.work.impl.model.Preference
import com.example.myapplication.AlarmReceiver
import com.example.myapplication.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import java.util.concurrent.TimeUnit

class HydrationFragment : Fragment() {

    private lateinit var preference: SharedPreferences
    private lateinit var preferenceEditor: SharedPreferences.Editor

    private lateinit var timeIntervalInputLayout: TextInputLayout
    private lateinit var timeIntervalAutoCompleteTextView: MaterialAutoCompleteTextView
    private lateinit var countDownloadProgress: CircularProgressIndicator
    private lateinit var countDownView: TextView

    private var countDownTimer: CountDownTimer? = null
    private var totalTimeMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        preference = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferenceEditor = preference.edit()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeIntervalInputLayout = view.findViewById(R.id.timeIntervalInputLayout)
        timeIntervalAutoCompleteTextView = timeIntervalInputLayout.editText as MaterialAutoCompleteTextView
        countDownloadProgress = view.findViewById(R.id.countDownloadProgress)
        countDownView = view.findViewById(R.id.countDownView)


        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            listOf(
                "10 Seconds",
                "15 Seconds",
                "20 Seconds ",
                "30 Minutes",
                "45 Minutes",
                "1 Hour",
                "1 Hour 30 Minutes",
                "2 Hour",
            )
        )
        timeIntervalAutoCompleteTextView.setAdapter(adapter)

        val savedInterval = preference.getString("selected_interval", "Not Selected")
        timeIntervalAutoCompleteTextView.setText(savedInterval, false)

        val currentTimeMillis = System.currentTimeMillis()
        val nextReminderTimeMillis = preference.getLong("next_reminder_time", 0L)
        val startTime = preference.getLong("current_time", 0L)

        if (nextReminderTimeMillis > currentTimeMillis) {
            totalTimeMillis = nextReminderTimeMillis - startTime
            val remainingTime = nextReminderTimeMillis - currentTimeMillis
            startCountdown(totalTimeMillis, remainingTime)
        } else if (nextReminderTimeMillis != 0L) {
            countDownView.text = "Time's up!"
            countDownloadProgress.progress = countDownloadProgress.max
        }

        timeIntervalAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            val newStartTime = System.currentTimeMillis()

            totalTimeMillis = when (selectedItem) {
                "10 Seconds(Testing)" -> 10 * 1000L
                "15 Seconds(Testing)" -> 15 * 1000L
                "20 Seconds(Testing)" -> 20 * 1000L
                "30 Minutes" -> 30 * 60 * 1000L
                "45 Minutes" -> 45 * 60 * 1000L
                "1 Hour" -> 60 * 60 * 1000L
                "1 Hour 30 Minutes" -> 90 * 60 * 1000L
                "2 Hour" -> 120 * 60 * 1000L
                else -> 0L
            }

            val newNextReminderTime = newStartTime + totalTimeMillis

            preferenceEditor
                .putLong("current_time", newStartTime)
                .putLong("next_reminder_time", newNextReminderTime)
                .putString("selected_interval", selectedItem)
                .apply()

            timeIntervalAutoCompleteTextView.setText(selectedItem, false)

            startCountdown(totalTimeMillis, totalTimeMillis)
            scheduleReminder(totalTimeMillis)
        }

    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleReminder(intervalMillis: Long) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        // Fire once at interval
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            pendingIntent
        )
    }

    private fun startCountdown(totalDuration: Long, remainingDuration: Long) {
        countDownTimer?.cancel()

        countDownloadProgress.max = (totalDuration / 1000).toInt()
        val progressDone = ((totalDuration - remainingDuration) / 1000).toInt()
        countDownloadProgress.progress = progressDone

        countDownTimer = object : CountDownTimer(remainingDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownView.text = formatTime(millisUntilFinished)
                val progress = ((totalDuration - millisUntilFinished) / 1000).toInt()
                countDownloadProgress.progress = progress
            }

            override fun onFinish() {
                countDownView.text = "Time's up!"
                countDownloadProgress.progress = countDownloadProgress.max
            }
        }.start()
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hydration, container, false)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "hydration_channel",
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to drink water"
            }

            val manager = requireContext().getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        fun newInstance() = HydrationFragment()
    }

}