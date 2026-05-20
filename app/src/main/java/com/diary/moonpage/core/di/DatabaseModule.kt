package com.diary.moonpage.core.di

import android.content.Context
import androidx.room.Room
import com.diary.moonpage.data.local.MoonPageDatabase
import com.diary.moonpage.data.local.dao.DailyLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MoonPageDatabase {
        return Room.databaseBuilder(
            context,
            MoonPageDatabase::class.java,
            MoonPageDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDailyLogDao(db: MoonPageDatabase): DailyLogDao {
        return db.dailyLogDao()
    }

    @Provides
    fun provideThemeDao(db: MoonPageDatabase): com.diary.moonpage.data.local.dao.ThemeDao {
        return db.themeDao()
    }

    @Provides
    fun provideCustomThemeDao(db: MoonPageDatabase): com.diary.moonpage.data.local.dao.CustomThemeDao {
        return db.customThemeDao()
    }

    @Provides
    fun provideStatisticsDao(db: MoonPageDatabase): com.diary.moonpage.data.local.dao.StatisticsDao {
        return db.statisticsDao()
    }
}
