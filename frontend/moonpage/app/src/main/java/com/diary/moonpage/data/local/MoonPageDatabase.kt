package com.diary.moonpage.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diary.moonpage.data.local.dao.DailyLogDao
import com.diary.moonpage.data.local.dao.ThemeDao
import com.diary.moonpage.data.local.entity.DailyLogEntity
import com.diary.moonpage.data.local.entity.ThemeEntity

@Database(entities = [DailyLogEntity::class, ThemeEntity::class], version = 2, exportSchema = false)
abstract class MoonPageDatabase : RoomDatabase() {
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun themeDao(): ThemeDao

    companion object {
        const val DATABASE_NAME = "moon_page_db"
    }
}
