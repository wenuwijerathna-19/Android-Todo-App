package com.example.myapplication.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.db.MoodPreference
import com.example.myapplication.model.MoodEntry
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MoodFragment : Fragment() {

    private lateinit var grid: GridView
    private lateinit var fab: View
    private lateinit var pref: MoodPreference
    private var displayYear = Calendar.getInstance().get(Calendar.YEAR)
    private var displayMonth = Calendar.getInstance().get(Calendar.MONTH) // 0-based
    private lateinit var textMonth: TextView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_mood, container, false)
        grid = v.findViewById(R.id.gridCalendar)
        fab = v.findViewById(R.id.fabAddMood)
        pref = MoodPreference(requireContext())

        textMonth = v.findViewById(R.id.textMonth)
        btnPrev = v.findViewById(R.id.btnPrevMonth)
        btnNext = v.findViewById(R.id.btnNextMonth)

        btnPrev.setOnClickListener {
            displayMonth -= 1
            if (displayMonth < 0) { displayMonth = 11; displayYear -= 1 }
            refreshCalendar()
        }
        btnNext.setOnClickListener {
            displayMonth += 1
            if (displayMonth > 11) { displayMonth = 0; displayYear += 1 }
            refreshCalendar()
        }

        refreshCalendar()

        fab.setOnClickListener {
            // Add mood for TODAY only — disallow adding for future dates
            val todayIso = getIsoForDay(Calendar.getInstance())
            // If the calendar is showing a future month (or the date is in the future) block
            val calShown = Calendar.getInstance()
            calShown.set(displayYear, displayMonth, 1)
            val now = Calendar.getInstance()
            if (calShown.get(Calendar.YEAR) > now.get(Calendar.YEAR) ||
                (calShown.get(Calendar.YEAR) == now.get(Calendar.YEAR) && calShown.get(Calendar.MONTH) > now.get(Calendar.MONTH))) {
                Toast.makeText(requireContext(), "Cannot add moods for future months", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bs = AddMoodBottomSheet.newInstance(todayIso)
            bs.onSaved = { entry ->
                pref.addOrUpdate(entry)
                refreshCalendar()
            }
            bs.show(childFragmentManager, "addMood")
        }

        return v
    }

    

    private fun refreshCalendar() {
        // Update header and calendar for displayYear/displayMonth
        val cal = Calendar.getInstance()
        cal.set(displayYear, displayMonth, 1)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        textMonth.text = sdf.format(cal.time)
        setupCalendarFor(displayYear, displayMonth)
    }

    private fun setupCalendarFor(year: Int, month: Int) {
        val days = generateMonthDays(year, month)
        val moods = pref.getForMonth(year, month)
        val map = mutableMapOf<String, MoodEntry>()
        moods.forEach { map[it.dateIso] = it }

        val adapter = object : BaseAdapter() {
            override fun getCount(): Int = days.size
            override fun getItem(position: Int): Any = days[position]
            override fun getItemId(position: Int): Long = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.item_mood_day, parent, false)
                val textDay = view.findViewById<TextView>(R.id.textDay)
                val textEmoji = view.findViewById<TextView>(R.id.textEmoji)
                val day = days[position]
                if (day.isBlank()) {
                    textDay.text = ""
                    textEmoji.text = ""
                } else {
                    textDay.text = day.takeWhile { it != '\n' }
                    val iso = day.substringAfter('\n')
                    val mood = map[iso]
                    textEmoji.text = mood?.emoji ?: ""
                }

                view.setOnClickListener {
                    val iso = day.substringAfter('\n')
                    // Disallow opening editor for future dates
                    val clickedCal = Calendar.getInstance()
                    val parts = iso.split('-')
                    if (parts.size >= 3) {
                        clickedCal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                    }
                    val now = Calendar.getInstance()
                    if (clickedCal.after(now)) {
                        Toast.makeText(requireContext(), "Cannot add or edit moods for future dates", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val bs = AddMoodBottomSheet.newInstance(iso)
                    bs.onSaved = { entry ->
                        pref.addOrUpdate(entry)
                        refreshCalendar()
                    }
                    bs.show(childFragmentManager, "editMood")
                }

                return view
            }
        }

        grid.adapter = adapter
    }

    

    private fun generateMonthDays(year: Int, monthZeroBased: Int): List<String> {
        val cal = Calendar.getInstance()
        cal.set(year, monthZeroBased, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1..7 (Sun..Sat)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val list = ArrayList<String>()
        // Add blanks for days before first day
        val blanks = (firstDayOfWeek - Calendar.SUNDAY) // align so that Sunday=0
        for (i in 0 until blanks) list.add("")

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (d in 1..daysInMonth) {
            cal.set(year, monthZeroBased, d)
            val iso = sdf.format(cal.time)
            list.add("$d\n$iso")
        }

        // Fill remaining cells to make full weeks
        while (list.size % 7 != 0) list.add("")
        return list
    }

    private fun getIsoForDay(cal: Calendar): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    // Bottom sheet defined inside to keep files small
    class AddMoodBottomSheet : BottomSheetDialogFragment() {
        var onSaved: ((MoodEntry) -> Unit)? = null
        private var dateIso: String = ""

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            dateIso = arguments?.getString("dateIso") ?: ""
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = BottomSheetDialog(requireContext())
            val v = layoutInflater.inflate(R.layout.bottom_sheet_add_mood, null)
            dialog.setContentView(v)

            val btns = listOf<Button>(
                v.findViewById(R.id.btnEmoji1),
                v.findViewById(R.id.btnEmoji2),
                v.findViewById(R.id.btnEmoji3),
                v.findViewById(R.id.btnEmoji4),
                v.findViewById(R.id.btnEmoji5)
            )

            var chosen = "🙂"
            // If there is an existing mood for this date, prefill
            val existing = try {
                MoodPreference(requireContext()).getForDate(dateIso).firstOrNull()
            } catch (e: Exception) { null }
            existing?.let {
                chosen = it.emoji
            }

            btns.forEach { b ->
                b.setOnClickListener { chosen = b.text.toString() }
            }

            val editNote = v.findViewById<EditText>(R.id.editNote)
            existing?.note?.let { editNote.setText(it) }

            val btnSave = v.findViewById<Button>(R.id.btnSaveMood)
            btnSave.setOnClickListener {
                val id = existing?.id ?: UUID.randomUUID().toString()
                val entry = MoodEntry(id = id, dateIso = dateIso, emoji = chosen, note = editNote.text.toString())
                onSaved?.invoke(entry)
                dismiss()
            }

            return dialog
        }

        companion object {
            fun newInstance(dateIso: String): AddMoodBottomSheet {
                val b = AddMoodBottomSheet()
                val args = Bundle()
                args.putString("dateIso", dateIso)
                b.arguments = args
                return b
            }
        }
    }

    companion object {
        fun newInstance(): MoodFragment = MoodFragment()
    }

}