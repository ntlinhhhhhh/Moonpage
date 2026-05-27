package com.diary.moonpage.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import com.diary.moonpage.domain.repository.ActivityRepository
import com.diary.moonpage.domain.repository.StatisticsRepository
import com.diary.moonpage.domain.repository.DailyLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: StatisticsRepository,
    private val activityRepository: ActivityRepository,
    private val logRepository: DailyLogRepository,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository,
    private val themeRepository: com.diary.moonpage.domain.repository.ThemeRepository,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            themePreferencesManager.themeType.collect { themeType ->
                _uiState.update { it.copy(themeType = themeType) }
                loadCustomMoods()
            }
        }
        viewModelScope.launch {
            themeRepository.activeTheme.collect {
                loadCustomMoods()
            }
        }
        viewModelScope.launch {
            userRepository.currentUser.collect { user ->
                _uiState.update { it.copy(gender = user?.gender) }
            }
        }
        loadStatistics()
        viewModelScope.launch {
            repository.refreshTrigger.collect {
                loadStatistics()
            }
        }
    }

    private suspend fun loadCustomMoods() {
        val activeThemeId = themeRepository.getActiveThemeId()
        if (activeThemeId != null) {
            val moodEntities = themeRepository.getMoodsForTheme(activeThemeId)
            if (moodEntities.isNotEmpty()) {
                val currentTheme = _uiState.value.themeType
                val customMoods = moodEntities.associate { entity ->
                    val level = when (entity.baseMoodId) {
                        "1", "Awful", "Very Sad" -> 1
                        "2", "Bad", "Sad" -> 2
                        "3", "Meh", "Neutral" -> 3
                        "4", "Good", "Happy" -> 4
                        "5", "Rad", "Very Happy" -> 5
                        else -> 3
                    }
                    
                    val parsedColor = MoonIcons.parseThemeColor(entity.iconUrl)
                    val color = parsedColor ?: MoonIcons.Moods.getMoodColor(level, currentTheme)

                    level to com.diary.moonpage.core.util.MoonIcon(
                        color = color,
                        name = entity.customName,
                        imageUrl = if (parsedColor == null && entity.iconUrl.isNotBlank()) entity.iconUrl else null,
                        drawableRes = if (parsedColor != null || entity.iconUrl.isBlank()) {
                            when (level) {
                                1 -> com.diary.moonpage.R.drawable.very_sad
                                2 -> com.diary.moonpage.R.drawable.sad
                                3 -> com.diary.moonpage.R.drawable.neutral
                                4 -> com.diary.moonpage.R.drawable.happy
                                5 -> com.diary.moonpage.R.drawable.very_happy
                                else -> com.diary.moonpage.R.drawable.neutral
                            }
                        } else null
                    )
                }
                _uiState.update { it.copy(customMoods = customMoods) }
            } else {
                _uiState.update { it.copy(customMoods = null) }
            }
        }
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val selectedYear = _uiState.value.selectedYear
                val selectedMonth = _uiState.value.selectedMonth

                val monthlyDeferred = async { fetchStatsData(selectedYear, selectedMonth, true) }
                val annualDeferred = async { fetchStatsData(selectedYear, selectedMonth, false) }

                val monthlyResult = runCatching { monthlyDeferred.await() }.getOrNull()
                val annualResult = runCatching { annualDeferred.await() }.getOrNull()

                _uiState.update { currentState ->
                    currentState.copy(
                        monthlyData = monthlyResult ?: currentState.monthlyData,
                        annualData = annualResult ?: currentState.annualData,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private suspend fun fetchStatsData(selectedYear: Int, selectedMonth: Int, isMonthly: Boolean): StatsData {
        val response = repository.getStatisticsSummary(
            selectedYear,
            if (isMonthly) selectedMonth else null,
            isMonthly
        )

        val allLogs = logRepository.getAllDailyLogsFlow().first()
        val periodLogs = ActivityInsightsEngine.filterLogsByPeriod(
            allLogs, selectedYear, if (isMonthly) selectedMonth else null, isMonthly
        )

        if (response.isSuccessful && response.body() != null) {
            val stats = response.body()!!
            val performedActivities = stats.performedActivityList()
            val deepDiveActivities = stats.deepDiveActivityList()
            val activityCategoriesById = buildActivityCategoriesById(performedActivities)
            val availableActivityCategories = performedActivities
                .mapNotNull { activityCategoriesById[it.activityId] }
                .toCollection(linkedSetOf())

            val freq = performedActivities.sortedByDescending { it.occurrence }.take(3)
            
            val targetIconId = _uiState.value.selectedIconId ?: deepDiveActivities.firstOrNull()?.activityId
            val deepDive = ActivityInsightsEngine.computeIconDeepDive(targetIconId, periodLogs, deepDiveActivities)

            val baseFilter = if (isMonthly) _uiState.value.monthlyData.activityFilter else _uiState.value.annualData.activityFilter
            val filtered = filterAndSortActivities(
                performedActivities,
                baseFilter.ifEmpty { availableActivityCategories },
                _uiState.value.sortOrder,
                activityCategoriesById
            )

            val avgWakeUpTime = stats.averageWakeupTime ?: if (stats.averageSleepStartTime != null && stats.averageSleepHours != null) {
                try {
                    val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH)
                    val date = sdf.parse(stats.averageSleepStartTime)
                    if (date != null) {
                        val cal = java.util.Calendar.getInstance().apply { time = date }
                        cal.add(java.util.Calendar.MINUTE, (stats.averageSleepHours * 60).toInt())
                        sdf.format(cal.time)
                    } else null
                } catch (e: Exception) { null }
            } else null

            return StatsData(
                stats = stats,
                frequentlyRecorded = freq,
                filteredActivities = filtered,
                activityFilter = availableActivityCategories,
                availableActivityCategories = availableActivityCategories,
                activityCategoriesById = activityCategoriesById,
                bestActivities = stats.bestActivities,
                worstActivities = stats.worstActivities.orEmpty(),
                bestCorrelations = emptyList(),
                worstCorrelations = emptyList(),
                iconDeepDive = deepDive,
                averageWakeUpTime = avgWakeUpTime
            )
        } else {
            throw Exception("Failed to load statistics")
        }
    }

    fun updateFilter(category: String, isSelected: Boolean) {
        _uiState.update { currentState ->
            val newFilterMonthly = if (isSelected) {
                currentState.monthlyData.activityFilter + category
            } else {
                currentState.monthlyData.activityFilter - category
            }
            
            val newFilterAnnual = if (isSelected) {
                currentState.annualData.activityFilter + category
            } else {
                currentState.annualData.activityFilter - category
            }
            
            val newMonthlyFiltered = filterAndSortActivities(
                currentState.monthlyData.stats?.performedActivityList() ?: emptyList(), 
                newFilterMonthly.ifEmpty { currentState.monthlyData.availableActivityCategories }, 
                currentState.sortOrder, 
                currentState.monthlyData.activityCategoriesById
            )
            
            val newAnnualFiltered = filterAndSortActivities(
                currentState.annualData.stats?.performedActivityList() ?: emptyList(), 
                newFilterAnnual.ifEmpty { currentState.annualData.availableActivityCategories }, 
                currentState.sortOrder, 
                currentState.annualData.activityCategoriesById
            )

            currentState.copy(
                monthlyData = currentState.monthlyData.copy(filteredActivities = newMonthlyFiltered, activityFilter = newFilterMonthly),
                annualData = currentState.annualData.copy(filteredActivities = newAnnualFiltered, activityFilter = newFilterAnnual)
            )
        }
    }

    fun toggleSortOrder() {
        _uiState.update { currentState ->
            val newSortOrder = if (currentState.sortOrder == SortOrder.MOST_RECORDED) {
                SortOrder.LEAST_RECORDED
            } else {
                SortOrder.MOST_RECORDED
            }
            
            val newMonthlyFiltered = filterAndSortActivities(
                currentState.monthlyData.stats?.performedActivityList() ?: emptyList(), 
                currentState.monthlyData.activityFilter.ifEmpty { currentState.monthlyData.availableActivityCategories }, 
                newSortOrder, 
                currentState.monthlyData.activityCategoriesById
            )
            val newAnnualFiltered = filterAndSortActivities(
                currentState.annualData.stats?.performedActivityList() ?: emptyList(), 
                currentState.annualData.activityFilter.ifEmpty { currentState.annualData.availableActivityCategories }, 
                newSortOrder, 
                currentState.annualData.activityCategoriesById
            )

            currentState.copy(
                sortOrder = newSortOrder,
                monthlyData = currentState.monthlyData.copy(filteredActivities = newMonthlyFiltered),
                annualData = currentState.annualData.copy(filteredActivities = newAnnualFiltered)
            )
        }
    }

    private fun filterAndSortActivities(
        activities: List<BestActivityDto>,
        filter: Set<String>,
        sortOrder: SortOrder,
        activityCategoriesById: Map<String, String>
    ): List<com.diary.moonpage.data.remote.dto.stats.BestActivityDto> {
        val filtered = activities.filter { activity ->
            activityCategoriesById[activity.activityId] in filter
        }
        return when (sortOrder) {
            SortOrder.MOST_RECORDED -> filtered.sortedByDescending { it.occurrence }
            SortOrder.LEAST_RECORDED -> filtered.sortedBy { it.occurrence }
        }
    }

    private suspend fun buildActivityCategoriesById(
        activities: List<BestActivityDto>
    ): Map<String, String> {
        val categoriesByRepositoryId = runCatching {
            activityRepository.activities.first()
                .associate { it.id to it.category.toFilterCategoryLabel() }
        }.getOrDefault(emptyMap())
        val fallbackCategoriesByName = MoonIcons.getAllCategories()
            .flatMap { (category, icons) ->
                icons.map { normalizeActivityName(it.name) to category.toFilterCategoryLabel() }
            }
            .toMap()

        return activities.associate { activity ->
            val category = categoriesByRepositoryId[activity.activityId]
                ?: fallbackCategoriesByName[normalizeActivityName(activity.activityName)]
                ?: "Other"
            activity.activityId to category
        }
    }

    private fun normalizeActivityName(activityName: String): String {
        return activityName.filterNot(Char::isWhitespace).lowercase()
    }

    private fun String.toFilterCategoryLabel(): String {
        return when (trim()) {
            "SelfCare", "Self-Care" -> "Self-Care"
            else -> trim()
        }
    }

    fun onIconClick(id: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedIconId = id) }
            
            val allLogs = logRepository.getAllDailyLogsFlow().first()
            val year = _uiState.value.selectedYear
            val month = _uiState.value.selectedMonth

            val monthlyLogs = ActivityInsightsEngine.filterLogsByPeriod(allLogs, year, month, true)
            val annualLogs = ActivityInsightsEngine.filterLogsByPeriod(allLogs, year, null, false)

            val monthlyDeepDive = ActivityInsightsEngine.computeIconDeepDive(id, monthlyLogs, _uiState.value.monthlyData.stats?.deepDiveActivityList() ?: emptyList())
            val annualDeepDive = ActivityInsightsEngine.computeIconDeepDive(id, annualLogs, _uiState.value.annualData.stats?.deepDiveActivityList() ?: emptyList())

            _uiState.update { 
                it.copy(
                    monthlyData = it.monthlyData.copy(iconDeepDive = monthlyDeepDive),
                    annualData = it.annualData.copy(iconDeepDive = annualDeepDive)
                ) 
            }
        }
    }

    fun onMonthSelected(year: Int, month: Int) {
        _uiState.update { it.copy(selectedYear = year, selectedMonth = month) }
        loadStatistics()
    }

    fun setMonthly(isMonthly: Boolean) {
        if (_uiState.value.isMonthly != isMonthly) {
            _uiState.update { it.copy(isMonthly = isMonthly) }
        }
    }

    fun shareRecapCard(context: android.content.Context, bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true) }
            try {
                com.diary.moonpage.core.util.ImageUtils.shareImage(context, bitmap, "My Year in Beans")
            } catch (e: Exception) {
                _uiState.update { it.copy(captureError = "Failed to share: ") }
            } finally {
                _uiState.update { it.copy(isCapturing = false) }
            }
        }
    }

    fun downloadRecapCard(context: android.content.Context, bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true) }
            try {
                com.diary.moonpage.core.util.ImageUtils.saveBitmapToGallery(context, bitmap)
            } catch (e: Exception) {
                _uiState.update { it.copy(captureError = "Failed to download: ") }
            } finally {
                _uiState.update { it.copy(isCapturing = false) }
            }
        }
    }

    fun saveRecapToGallery(context: android.content.Context, bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true) }
            try {
                com.diary.moonpage.core.util.ImageUtils.saveBitmapToGallery(context, bitmap)
                com.diary.moonpage.core.util.ImageUtils.shareImage(context, bitmap, "My Year in Beans")
            } catch (e: Exception) {
                _uiState.update { it.copy(captureError = "Failed to save: ") }
            } finally {
                _uiState.update { it.copy(isCapturing = false) }
            }
        }
    }

    fun clearCaptureError() {
        _uiState.update { it.copy(captureError = null) }
    }

    private fun StatisticsResponse.performedActivityList(): List<BestActivityDto> {
        return performedActivities.orEmpty()
    }

    private fun StatisticsResponse.deepDiveActivityList(): List<BestActivityDto> {
        val performed = performedActivityList()
        return if (performed.isNotEmpty()) {
            performed
        } else {
            (bestActivities + worstActivities.orEmpty()).distinctBy { it.activityId }
        }
    }
}
