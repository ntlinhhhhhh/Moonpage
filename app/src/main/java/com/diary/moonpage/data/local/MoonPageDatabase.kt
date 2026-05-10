package com.diary.moonpage.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.diary.moonpage.data.local.dao.DailyLogDao
import com.diary.moonpage.data.local.dao.ThemeDao
import com.diary.moonpage.data.local.dao.StatisticsDao
import com.diary.moonpage.data.local.entity.DailyLogEntity
import com.diary.moonpage.data.local.entity.ThemeEntity
import com.diary.moonpage.data.local.entity.StatisticsEntity
import com.diary.moonpage.data.local.entity.ThemeMoodEntity

@Database(
    entities = [DailyLogEntity::class, ThemeEntity::class, StatisticsEntity::class, ThemeMoodEntity::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MoonPageDatabase : RoomDatabase() {
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun themeDao(): ThemeDao
    abstract fun statisticsDao(): StatisticsDao

    companion object {
        const val DATABASE_NAME = "moon_page_db"
    }
}
