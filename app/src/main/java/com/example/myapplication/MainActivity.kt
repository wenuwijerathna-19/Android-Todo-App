package com.example.myapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val testBtn: Button = findViewById(R.id.test)

        testBtn.setOnClickListener {

            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent: Intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            // Set the alarm to trigger after 5 seconds
            val triggerTime = System.currentTimeMillis() + (10 * 1000)

            // For a one-time alarm that wakes up the device
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

        }

    }



}