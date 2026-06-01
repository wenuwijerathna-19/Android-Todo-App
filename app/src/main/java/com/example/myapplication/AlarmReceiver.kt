package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    @SuppressLint("ScheduleExactAlarm", "CommitPrefEdits")
    override fun onReceive(p0: Context?, p1: Intent?) {

        val notificationManager = p0!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(p0, "hydration_channel")
            .setContentTitle("Hydration Reminder")
            .setContentText("Don’t Forget Your Water 💧")
            .setSmallIcon(R.drawable.icon_water_drop)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())

        val prefs = PreferenceManager.getDefaultSharedPreferences(p0)
        val editor = prefs.edit()
        val selectedInterval = prefs.getString("selected_interval", "30 Minutes")
        val intervalMillis = when (selectedInterval) {
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

        editor
            .putLong("current_time", System.currentTimeMillis())
            .putLong("next_reminder_time", System.currentTimeMillis() + intervalMillis)
            .apply()

        val alarmManager = p0.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val newIntent = Intent(p0, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            p0,
            0,
            newIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalMillis, pendingIntent)

    }

}