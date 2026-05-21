package com.diary.moonpage.core.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.diary.moonpage.service.WeatherAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun ensureWeatherAlarmScheduled() {
        if (hasScheduledAlarm()) return
        scheduleNextWeatherCheck(INITIAL_DELAY_MILLIS)
    }

    fun scheduleNextWeatherCheck(delayMillis: Long = CHECK_INTERVAL_MILLIS) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = System.currentTimeMillis() + delayMillis
        val pendingIntent = createPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelWeatherChecks() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        createPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT)?.let { pendingIntent ->
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun hasScheduledAlarm(): Boolean {
        return createPendingIntent(PendingIntent.FLAG_NO_CREATE) != null
    }

    private fun createPendingIntent(flags: Int): PendingIntent? {
        val intent = Intent(context, WeatherAlarmReceiver::class.java).apply {
            action = WeatherAlarmReceiver.ACTION_CHECK_WEATHER
        }
        return PendingIntent.getBroadcast(
            context,
            WEATHER_ALARM_REQUEST_CODE,
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val WEATHER_ALARM_REQUEST_CODE = 2001
        private const val INITIAL_DELAY_MILLIS = 15 * 60 * 1000L
        const val CHECK_INTERVAL_MILLIS = 3 * 60 * 60 * 1000L
    }
}
