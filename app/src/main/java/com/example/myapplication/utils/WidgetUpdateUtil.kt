package com.example.myapplication.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.example.myapplication.HabitWidget

object WidgetUpdateUtil {
    
    /**
     * Updates all instances of HabitWidget to reflect current habit completion percentage
     */
    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, HabitWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        if (appWidgetIds.isNotEmpty()) {
            // Create a HabitWidget instance to trigger the update
            val habitWidget = HabitWidget()
            habitWidget.onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}