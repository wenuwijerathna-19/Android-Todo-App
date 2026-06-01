package com.example.myapplication

import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.HabitAdapter
import com.example.myapplication.db.HabitPreference
import com.example.myapplication.fragment.DailyHabitFragment
import com.example.myapplication.fragment.HabitAddBottomSheetFragment
import com.example.myapplication.fragment.HydrationFragment
import com.example.myapplication.fragment.MoodFragment
import com.example.myapplication.model.Habit
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator

class HabitActivity : AppCompatActivity() {

    private val requestNotificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Notifications are disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_habit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        bottomNavView = findViewById(R.id.bottomNavView)

        bottomNavView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> {
                    loadFragment(DailyHabitFragment.newInstance())
                    true
                }
                R.id.mood -> {
                    loadFragment(MoodFragment.newInstance())
                    true
                }
                R.id.hydration -> {
                    loadFragment(HydrationFragment.newInstance())
                    true
                }
                else -> {
                    false
                }
            }
        }

        loadFragment(DailyHabitFragment.newInstance())

    }

    fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,fragment).commit()
    }

}