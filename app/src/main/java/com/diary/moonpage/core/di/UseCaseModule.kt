package com.diary.moonpage.core.di

import android.content.Context
import com.diary.moonpage.core.util.SettingsPreferencesManager
import com.diary.moonpage.core.util.UserManager
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.domain.repository.MomentRepository
import com.diary.moonpage.domain.repository.NotificationRepository
import com.diary.moonpage.domain.repository.StatisticsRepository
import com.diary.moonpage.domain.usecase.auth.ValidateEmailUseCase
import com.diary.moonpage.domain.usecase.auth.ValidatePasswordUseCase
import com.diary.moonpage.domain.usecase.auth.ValidateUsernameUseCase
import com.diary.moonpage.domain.usecase.moment.*
import com.diary.moonpage.domain.usecase.notification.CheckAndTriggerNotificationsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideValidateEmailUseCase(): ValidateEmailUseCase = ValidateEmailUseCase()

    @Provides
    @Singleton
    fun provideValidatePasswordUseCase(): ValidatePasswordUseCase = ValidatePasswordUseCase()

    @Provides
    @Singleton
    fun provideValidateUsernameUseCase(): ValidateUsernameUseCase = ValidateUsernameUseCase()

    @Provides
    @Singleton
    fun provideGetMyMomentsUseCase(repository: MomentRepository): GetMyMomentsUseCase = GetMyMomentsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetMomentUseCase(repository: MomentRepository): GetMomentUseCase = GetMomentUseCase(repository)

    @Provides
    @Singleton
    fun provideUploadMomentUseCase(repository: MomentRepository): UploadMomentUseCase = UploadMomentUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteMomentUseCase(repository: MomentRepository): DeleteMomentUseCase = DeleteMomentUseCase(repository)

    @Provides
    @Singleton
    fun provideCheckAndTriggerNotificationsUseCase(
        notificationRepository: NotificationRepository,
        statsRepository: StatisticsRepository,
        dailyLogRepository: DailyLogRepository,
        userManager: UserManager,
        settingsPreferencesManager: SettingsPreferencesManager,
        @ApplicationContext context: Context
    ): CheckAndTriggerNotificationsUseCase = 
        CheckAndTriggerNotificationsUseCase(
            notificationRepository,
            statsRepository,
            dailyLogRepository,
            userManager,
            settingsPreferencesManager,
            context
        )
}
