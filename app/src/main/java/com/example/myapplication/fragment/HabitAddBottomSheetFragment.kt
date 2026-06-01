package com.example.myapplication.fragment

import android.appwidget.AppWidgetManager
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TimePicker
import androidx.fragment.app.FragmentManager
import com.example.myapplication.R
import com.example.myapplication.db.HabitPreference
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class HabitAddBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var habitPreference: HabitPreference
    var listener: Listener? = null

    private lateinit var timePicker: TimePicker
    private lateinit var addBtn: Button
    private lateinit var nameInput: TextInputLayout
    private lateinit var nameInputEditText: TextInputEditText
    private var editMode: Boolean = false
    private var editOldTime: Long = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        habitPreference = HabitPreference(requireContext())

        timePicker = view.findViewById(R.id.timePicker)
        nameInput = view.findViewById(R.id.nameInput)
        nameInputEditText = nameInput.editText as TextInputEditText
        addBtn = view.findViewById(R.id.addBtn)

        // prefill if args present
        arguments?.let { args ->
            val prefillName = args.getString("prefill_name")
            val prefillHour = args.getInt("prefill_hour", -1)
            val prefillMinute = args.getInt("prefill_minute", -1)
            if(!prefillName.isNullOrEmpty()){
                nameInputEditText.setText(prefillName)
            }
            if(prefillHour >= 0 && prefillMinute >= 0){
                timePicker.hour = prefillHour
                timePicker.minute = prefillMinute
            }
        }

        addBtn.setOnClickListener {

            val name = nameInputEditText.text.toString()
            //validate
            var isValidated = true
            if(TextUtils.isEmpty(name)){
                isValidated = false
                nameInput.error = "Please enter habit name."
            }else{
                nameInput.error = null
            }

            if(!isValidated){
                return@setOnClickListener
            }

            val calendar: Calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val success = if(editMode && editOldTime != -1L){
                habitPreference.update(editOldTime, name, calendar.timeInMillis)
            }else{
                habitPreference.insert(name,calendar.timeInMillis)
            }

            if(!success){
                // show error to user about duplicate time
                nameInput.error = "A habit already exists for the selected time. Please choose another time."
                return@setOnClickListener
            }

            nameInput.error = null
            listener?.onAdded()
            dismiss()

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_habit,container,false)
    }

    companion object{

        fun show(fragmentManager: FragmentManager, listener: Listener){
            val habitAddBottomSheetFragment = HabitAddBottomSheetFragment()
            habitAddBottomSheetFragment.listener = listener
            habitAddBottomSheetFragment.show(fragmentManager,"HabitAddBottomSheetFragment")
        }

        fun showForEdit(fragmentManager: FragmentManager, listener: Listener, oldTime: Long, name: String, hourOfDay: Int, minute: Int){
            val fragment = HabitAddBottomSheetFragment()
            fragment.listener = listener
            fragment.editMode = true
            fragment.editOldTime = oldTime
            // prefill name and time after view created
            fragment.arguments = Bundle().apply {
                putString("prefill_name", name)
                putInt("prefill_hour", hourOfDay)
                putInt("prefill_minute", minute)
            }
            fragment.show(fragmentManager, "HabitAddBottomSheetFragment")
        }

    }

    interface Listener{
        fun onAdded()
    }

}