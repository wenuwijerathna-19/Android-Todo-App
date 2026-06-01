package com.example.myapplication.db

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.example.myapplication.model.Habit
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.toString

class HabitPreference(val context: Context) {

    private var preference: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var preferenceEditor: SharedPreferences.Editor = preference.edit()

    fun getAllHabits(): List<Habit> {
        val data = preference.getString("habits", "[]")
        val currentData = JSONArray(data)
        val list: MutableList<Habit> = mutableListOf()

        for (i in 0 until currentData.length()) {
            val obj = currentData.getJSONObject(i)
            list.add(Habit(obj.getString("name"), obj.getLong("time")))
        }

        return list.sortedBy { it.time }
    }

    fun insert(name: String, timeInMillis: Long): Boolean{

        val data = preference.getString("habits","[]")
        val currentData = JSONArray(data)

        // check for duplicate time
        for (i in 0 until currentData.length()){
            val obj = currentData.getJSONObject(i)
            if(obj.optLong("time", -1L) == timeInMillis){
                return false
            }
        }

        val newObj = JSONObject()
        newObj.put("time",timeInMillis)
        newObj.put("name",name)
        currentData.put(newObj)

        preferenceEditor.putString("habits",currentData.toString())
        preferenceEditor.commit()
        return true

    }

    fun update(oldTime: Long, newName: String, newTimeInMillis: Long): Boolean{

        val data = preference.getString("habits","[]")
        val currentData = JSONArray(data)

        for (i in 0 until currentData.length()){
            val obj = currentData.getJSONObject(i)
            val t = obj.optLong("time", -1L)
            if(t == newTimeInMillis && t != oldTime){
                return false
            }
        }

        val newJsonArray = JSONArray()
        for(i in 0 until currentData.length()){
            val currentObject = currentData.getJSONObject(i)
            if(currentObject.getLong("time") == oldTime){
                val newObj = JSONObject()
                newObj.put("time", newTimeInMillis)
                newObj.put("name", newName)
                newJsonArray.put(newObj)
            }else{
                newJsonArray.put(currentObject)
            }
        }

        preferenceEditor.putString("habits",newJsonArray.toString())
        preferenceEditor.commit()
        return true

    }

    fun setCompletion(habitTime: Long, isoDate: String, completed: Boolean){
        val data = preference.getString("habit_completions","{}")
        val root = JSONObject(data)

        val key = habitTime.toString()
        val obj = if(root.has(key)) root.getJSONObject(key) else JSONObject()

        obj.put(isoDate, completed)
        root.put(key, obj)

        preferenceEditor.putString("habit_completions", root.toString())
        preferenceEditor.commit()
    }

    fun getCompletion(): Map<String, Double> {
        val data = preference.getString("habit_completions", "{}")
        val root = JSONObject(data)
        // Calculate completion percentage per date using total number of habits as denominator.
        // This ensures missing entries (no stored value) count as incomplete for that day.
        val allHabits = getAllHabits()
        val totalHabits = allHabits.size

        val dateMap = mutableMapOf<String, Int>()

        // Iterate over stored completions and count completed per date
        val habitKeys = root.keys()
        while (habitKeys.hasNext()) {
            val habitKey = habitKeys.next()
            val habitObj = root.getJSONObject(habitKey)

            val dates = habitObj.keys()
            while (dates.hasNext()) {
                val date = dates.next()
                val completed = habitObj.optBoolean(date, false)
                if (completed) {
                    dateMap[date] = dateMap.getOrDefault(date, 0) + 1
                } else {
                    // ensure the date exists in the map so we later compute 0% if no completions
                    if (!dateMap.containsKey(date)) dateMap[date] = dateMap.getOrDefault(date, 0)
                }
            }
        }

        // If there are no habits, return an empty map
        if (totalHabits == 0) return emptyMap()

        // Build result map with percentages; include dates present in stored completions.
        val result = dateMap.mapValues { (_, done) ->
            (done.toDouble() / totalHabits.toDouble()) * 100.0
        }

        // Return a map sorted by date (ascending) so chart shows chronological order
        return result.toSortedMap()
    }

    fun getTodayCompletionPercentage(): Double {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val allCompletion = getCompletion()
        return allCompletion[today] ?: 0.0
    }



    fun isCompleted(habitTime: Long, isoDate: String): Boolean{
        val data = preference.getString("habit_completions","{}")
        val root = JSONObject(data)
        val key = habitTime.toString()
        if(!root.has(key)) return false
        val obj = root.getJSONObject(key)
        return obj.optBoolean(isoDate, false)
    }

    fun delete(time: Long){

        val data = preference.getString("habits","[]")
        val currentData = JSONArray(data)
        val newJsonArray = JSONArray()
        //find time slot and delete it
        for(i in 0 until currentData.length()){
            val currentObject = currentData.getJSONObject(i)
            if(currentObject.getLong("time") != time){
                newJsonArray.put(currentObject)
            }
        }

        preferenceEditor.putString("habits",newJsonArray.toString())
        preferenceEditor.commit()

    }

}