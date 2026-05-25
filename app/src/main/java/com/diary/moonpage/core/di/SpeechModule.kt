package com.diary.moonpage.core.di

import android.content.Context
import com.diary.moonpage.core.util.SpeechToTextManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SpeechModule {

    @Provides
    @Singleton
    fun provideSpeechToTextManager(
        @ApplicationContext context: Context
    ): SpeechToTextManager {
        return SpeechToTextManager(context)
    }
}
