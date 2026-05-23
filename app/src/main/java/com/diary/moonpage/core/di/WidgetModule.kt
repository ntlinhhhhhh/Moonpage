package com.diary.moonpage.core.di

import android.content.Context
import com.diary.moonpage.core.util.WidgetPreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WidgetModule {

    @Provides
    @Singleton
    fun provideWidgetPreferencesManager(
        @ApplicationContext context: Context
    ): WidgetPreferencesManager {
        return WidgetPreferencesManager(context)
    }
}
