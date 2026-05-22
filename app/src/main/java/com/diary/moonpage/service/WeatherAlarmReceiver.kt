package com.diary.moonpage.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.diary.moonpage.core.util.WeatherAlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WeatherAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var weatherAlarmScheduler: WeatherAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_CHECK_WEATHER) return

        enqueueImmediateWeatherCheck(context)
        weatherAlarmScheduler.scheduleNextWeatherCheck()
    }

    private fun enqueueImmediateWeatherCheck(context: Context) {
        val request = OneTimeWorkRequestBuilder<WeatherWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WEATHER_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        const val ACTION_CHECK_WEATHER = "com.diary.moonpage.action.CHECK_WEATHER"
        private const val WEATHER_WORK_NAME = "ImmediateWeatherCheck"
    }
}
