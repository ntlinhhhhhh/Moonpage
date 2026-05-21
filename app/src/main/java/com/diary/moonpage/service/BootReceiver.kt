package com.diary.moonpage.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.diary.moonpage.core.util.ReminderManager
import com.diary.moonpage.core.util.SettingsPreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var settingsPreferencesManager: SettingsPreferencesManager
    
    @Inject
    lateinit var reminderManager: ReminderManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            runBlocking {
                if (settingsPreferencesManager.isReminderEnabled.first()) {
                    val timeStr = settingsPreferencesManager.reminderTime.first()
                    val time = timeStr.split(":")
                    if (time.size == 2) {
                        reminderManager.scheduleDailyReminder(time[0].toInt(), time[1].toInt())
                    }
                }
            }
        }
    }
}
