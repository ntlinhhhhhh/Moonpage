package com.diary.moonpage.ui.screens.stats

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.core.util.ThemePreferencesManager
import com.diary.moonpage.data.local.entity.DailyLogEntity
import com.diary.moonpage.data.local.entity.ThemeEntity
import com.diary.moonpage.data.local.entity.ThemeMoodEntity
import com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import com.diary.moonpage.domain.model.Activity
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.User
import com.diary.moonpage.domain.repository.ActivityRepository
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.domain.repository.StatisticsRepository
import com.diary.moonpage.domain.repository.ThemeRepository
import com.diary.moonpage.domain.repository.UserRepository
import com.diary.moonpage.domain.repository.CreateThemePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class StatisticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: StatisticsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadStatsData_calculatesCorrectStreak_and_MoodDistribution() = runTest {
        val fakeStatsRepository = object : StatisticsRepository {
            override val refreshTrigger: kotlinx.coroutines.flow.SharedFlow<Unit> = MutableSharedFlow()
            override fun triggerRefresh() {}
            override suspend fun getGlobalSummary(): Response<StatisticsResponse> = Response.success(null)
            override suspend fun clearCache() {}

            override suspend fun getStatisticsSummary(year: Int, month: Int?, isMonthly: Boolean): Response<StatisticsResponse> {
                return Response.success(
                    StatisticsResponse(
                        totalLogs = 10,
                        totalPhotos = 5,
                        currentStreak = 3,
                        longestStreak = 5,
                        moodDistribution = listOf(
                            MoodDistributionDto(label = "Happy", count = 5, percentage = 50.0),
                            MoodDistributionDto(label = "Sad", count = 5, percentage = 50.0)
                        ),
                        moodFlow = emptyList(),
                        bestActivities = emptyList(),
                        worstActivities = emptyList(),
                        performedActivities = emptyList()
                    )
                )
            }
        }

        val fakeActivityRepo = object : ActivityRepository {
            override val activities: Flow<List<Activity>> = flowOf(emptyList())
            override suspend fun syncActivities(): Result<Unit> = Result.success(Unit)
            override suspend fun clearCache() {}
        }

        val fakeLogRepo = object : DailyLogRepository {
            override suspend fun createDailyLog(baseMoodId: Int, date: String, note: String?, sleepHours: Double?, sleepStartTime: String?, isMenstruation: Boolean, menstruationPhase: String?, activityIds: List<String>?, dailyPhotos: List<java.io.File>?, steps: Int?, musicTitle: String?, artistName: String?, albumArtUrl: String?, calories: Int?, distance: Double?, wakeupTime: String?, weather: String?, temperature: Double?): Result<Unit> = Result.success(Unit)
            override suspend fun getDailyLogByDate(date: String): Result<DailyLog> = Result.success(DailyLog(id="1", baseMoodId=3, date="2023-10-01", note=null, sleepHours=null, sleepStartTime=null, isMenstruation=false, menstruationPhase=null, steps=null, musicRecord=null, musicTitle=null, artistName=null, albumArtUrl=null, dailyPhotos=null, activityIds=null, createdAt=null, calories=null, distance=null, wakeupTime=null, weather=null, temperature=null))
            override suspend fun cacheDailyLog(log: DailyLog) {}
            override fun getDailyLogByDateFlow(date: String): Flow<DailyLog?> = flowOf(null)
            override suspend fun deleteDailyLog(date: String): Result<Unit> = Result.success(Unit)
            override fun getDailyLogsByMonth(yearMonth: String): Flow<List<DailyLog>> = flowOf(emptyList())
            override suspend fun clearCache() {}
            override fun getAllDailyLogsFlow(): Flow<List<DailyLog>> = flowOf(emptyList())
        }

        val emptyUser = User("", "", "", "", null, null, null)

        val fakeUserRepo = object : UserRepository {
            override val currentUser: StateFlow<User?> = MutableStateFlow(null)
            override val localAvatarPath: Flow<String?> = flowOf(null)
            override suspend fun getCurrentUser(): Result<User> = Result.success(emptyUser)
            override suspend fun updateProfile(request: com.diary.moonpage.data.remote.dto.auth.UpdateProfileRequestDto): Result<User> = Result.success(emptyUser)
            override suspend fun getMyThemes(): Result<List<Theme>> = Result.success(emptyList())
            override suspend fun deleteUser(id: String): Result<Unit> = Result.success(Unit)
            override suspend fun updateAvatar(image: okhttp3.MultipartBody.Part, localFile: java.io.File): Result<User> = Result.success(emptyUser)
            override suspend fun clearCache() {}
            override suspend fun updateLanguage(language: String): Result<Unit> = Result.success(Unit)
            override suspend fun deleteMyAccount(): Result<Unit> = Result.success(Unit)
            override suspend fun changePassword(old: String, new: String): Result<Unit> = Result.success(Unit)
            override suspend fun confirmPassword(password: String?, googleIdToken: String?): Result<Unit> = Result.success(Unit)
            override suspend fun buyStreakFreeze(): Result<Unit> = Result.success(Unit)
            override suspend fun recoverStreak(): Result<Unit> = Result.success(Unit)
            override suspend fun spendCoinsLocally(amount: Int): Result<User> = Result.success(emptyUser)
        }

        val fakeThemeRepo = object : ThemeRepository {
            override val activeTheme: Flow<Theme?> = flowOf(null)
            override val ownedThemes: Flow<List<Theme>> = flowOf(emptyList())
            override val allThemes: Flow<List<Theme>> = flowOf(emptyList())
            override val myThemes: Flow<List<Theme>> = flowOf(emptyList())
            override suspend fun getAllThemes(): Result<List<Theme>> = Result.success(emptyList())
            override suspend fun getOwnedThemes(): Result<List<Theme>> = Result.success(emptyList())
            override suspend fun getMyThemes(): Result<List<Theme>> = Result.success(emptyList())
            override suspend fun createThemes(themes: List<CreateThemePayload>): Result<Unit> = Result.success(Unit)
            override suspend fun renameTheme(themeId: String, name: String): Result<Unit> = Result.success(Unit)
            override suspend fun buyTheme(themeId: String, price: Int?): Result<Unit> = Result.success(Unit)
            override suspend fun setActiveTheme(themeId: String): Result<Unit> = Result.success(Unit)
            override suspend fun getMoodsForTheme(themeId: String): List<ThemeMoodEntity> = emptyList()
            override suspend fun getActiveThemeId(): String? = null
            override suspend fun clearCache() {}
        }

        val themeManager = ThemePreferencesManager(ApplicationProvider.getApplicationContext())

        viewModel = StatisticsViewModel(
            fakeStatsRepository,
            fakeActivityRepo,
            fakeLogRepo,
            fakeUserRepo,
            fakeThemeRepo,
            themeManager
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        val monthlyStats = state.monthlyData.stats

        assertEquals(3, monthlyStats?.currentStreak)
        assertEquals(5, monthlyStats?.longestStreak)
        assertEquals(2, monthlyStats?.moodDistribution?.size)
        assertEquals(50.0, monthlyStats?.moodDistribution?.first()?.percentage)
    }
}
