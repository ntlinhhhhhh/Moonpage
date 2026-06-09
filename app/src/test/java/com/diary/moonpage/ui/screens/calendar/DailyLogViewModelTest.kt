package com.diary.moonpage.ui.screens.calendar

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.core.util.*
import com.diary.moonpage.data.remote.api.SpotifyApi
import com.diary.moonpage.domain.model.Activity
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.model.User
import com.diary.moonpage.domain.repository.*
import com.diary.moonpage.domain.usecase.notification.CheckAndTriggerNotificationsUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DailyLogViewModelTest {

    private lateinit var viewModel: DailyLogViewModel
    
    // Dependencies
    private val repository: DailyLogRepository = mockk(relaxed = true)
    private val themeRepository: ThemeRepository = mockk(relaxed = true)
    private val weatherRepository: WeatherRepository = mockk(relaxed = true)
    private val locationTracker: LocationTracker = mockk(relaxed = true)
    private val activityPreferencesManager: ActivityPreferencesManager = mockk(relaxed = true)
    private val themePreferencesManager: ThemePreferencesManager = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val tokenManager: TokenManager = mockk(relaxed = true)
    private val statisticsRepository: StatisticsRepository = mockk(relaxed = true)
    private val spotifyApi: SpotifyApi = mockk(relaxed = true)
    private val momentRepository: MomentRepository = mockk(relaxed = true)
    private val checkAndTriggerNotificationsUseCase: CheckAndTriggerNotificationsUseCase = mockk(relaxed = true)
    private val dailyLogPhotoManager: DailyLogPhotoManager = mockk(relaxed = true)
    private val healthConnectManager: HealthConnectManager = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val speechToTextManager: SpeechToTextManager = mockk(relaxed = true)
    private val languagePreferencesManager: LanguagePreferencesManager = mockk(relaxed = true)
    private val settingsPreferencesManager: SettingsPreferencesManager = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default flows
        every { activityPreferencesManager.enabledCategories } returns MutableStateFlow(emptySet<String>())
        every { activityPreferencesManager.activities } returns MutableStateFlow(emptyList<Activity>())
        every { themePreferencesManager.themeType } returns MutableStateFlow(MoonThemeType.DEFAULT)
        coEvery { themeRepository.activeTheme } returns flowOf(null)
        every { tokenManager.getSpotifyToken() } returns flowOf(null)
        every { userRepository.currentUser } returns MutableStateFlow<User?>(null)
        every { settingsPreferencesManager.isMusicEnabled } returns flowOf(true)
        every { settingsPreferencesManager.isSleepEnabled } returns flowOf(true)
        every { settingsPreferencesManager.isStepsEnabled } returns flowOf(true)
        every { settingsPreferencesManager.isMenstruationEnabled } returns flowOf(true)
        every { dailyLogPhotoManager.getLocalPaths() } returns flowOf(emptyMap<String, String>())
        
        // Fix for internal launch coroutine in init block
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any<String>(), any<String>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>(), any()) } returns 0

        viewModel = DailyLogViewModel(
            repository = repository,
            themeRepository = themeRepository,
            weatherRepository = weatherRepository,
            locationTracker = locationTracker,
            activityPreferencesManager = activityPreferencesManager,
            themePreferencesManager = themePreferencesManager,
            userRepository = userRepository,
            tokenManager = tokenManager,
            statisticsRepository = statisticsRepository,
            spotifyApi = spotifyApi,
            momentRepository = momentRepository,
            checkAndTriggerNotificationsUseCase = checkAndTriggerNotificationsUseCase,
            dailyLogPhotoManager = dailyLogPhotoManager,
            applicationScope = testScope,
            healthConnectManager = healthConnectManager,
            context = context,
            speechToTextManager = speechToTextManager,
            languagePreferencesManager = languagePreferencesManager,
            settingsPreferencesManager = settingsPreferencesManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test onEvent OnMoodSelected updates uiState correctly`() = runTest {
        // Arrange
        val newMoodId = 4

        // Act
        viewModel.onEvent(DailyLogUiEvent.OnMoodSelected(newMoodId))

        // Assert
        assertEquals(newMoodId, viewModel.uiState.value.selectedMood)
    }

    @Test
    fun `test onEvent OnNoteChanged updates noteText correctly`() = runTest {
        // Arrange
        val note = "This is a test note"

        // Act
        viewModel.onEvent(DailyLogUiEvent.OnNoteChanged(note))

        // Assert
        assertEquals(note, viewModel.uiState.value.noteText)
    }

    @Test
    fun `test OnImportSteps updates state when Health Connect is available and permission is granted`() = runTest {
        // Arrange
        coEvery { healthConnectManager.getSdkStatus() } returns HealthConnectClient.SDK_AVAILABLE
        coEvery { healthConnectManager.hasAllPermissions() } returns true
        val mockData = HealthData(
            steps = 5000,
            calories = 300,
            distance = 2.5,
            sleepHours = 7.5,
            sleepStartTime = "22:00",
            sleepWakeTime = "05:30"
        )
        coEvery { healthConnectManager.readHealthData(any()) } returns mockData

        // Act
        viewModel.onEvent(DailyLogUiEvent.OnImportSteps)
        testDispatcher.scheduler.advanceUntilIdle() // Process coroutines

        // Assert
        val state = viewModel.uiState.value
        assertEquals(5000, state.steps)
        assertEquals(300, state.calories)
        assertEquals(2.5, state.distance, 0.001)
        assertFalse(state.isImportingHealth)
    }
}
