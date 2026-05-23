package com.diary.moonpage.widget.glance

import com.diary.moonpage.core.util.ActivityPreferencesManager
import com.diary.moonpage.core.util.ThemePreferencesManager
import com.diary.moonpage.core.util.UserManager
import com.diary.moonpage.core.util.WidgetPreferencesManager
import com.diary.moonpage.domain.repository.DailyLogRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MoonpageWidgetEntryPoint {
    fun dailyLogRepository(): DailyLogRepository
    fun userManager(): UserManager
    fun themePreferencesManager(): ThemePreferencesManager
    fun activityPreferencesManager(): ActivityPreferencesManager
    fun widgetPreferencesManager(): WidgetPreferencesManager
}
