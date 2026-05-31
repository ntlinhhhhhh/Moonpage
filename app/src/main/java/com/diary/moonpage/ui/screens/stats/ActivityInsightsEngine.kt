package com.diary.moonpage.ui.screens.stats

import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.domain.model.DailyLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

// ===========================
// Data Models for computed insights
// ===========================

data class ActivityCorrelation(
    val activityId: String,
    val activityName: String,
    val occurrence: Int,
    val bestRatePercent: Int,    // % of days with this activity that had Best mood (4-5)
    val worstRatePercent: Int,   // % of days with this activity that had Worst mood (1-2)
    val averageMoodScore: Double
)

data class MoodDistributionEntry(
    val moodId: Int,              // 1-5
    val count: Int,
    val percentage: Float
)

data class IconDeepDiveResult(
    val activityId: String,
    val activityName: String,
    val totalOccurrence: Int,
    val averageMoodScore: Double,
    val moodDistribution: List<MoodDistributionEntry>, // sorted moodId 1..5
    val longestStreak: Int,                            // consecutive days using this activity
    val weeklyFrequency: Double,                       // average times per week
    val relatedActivities: List<Pair<String, Int>>     // activityName -> co-occurrence count, top 3
)

// ===========================
// Computation Engine
// ===========================

object ActivityInsightsEngine {

    private const val MIN_OCCURRENCE_THRESHOLD = 3  // Minimum times an activity must appear

    /**
     * Compute Best & Worst activity correlations.
     *
     * Best = activities with highest P(mood 4-5 | activity used)
     * Worst = activities with highest P(mood 1-2 | activity used)
     *
     * Only includes activities with occurrence >= MIN_OCCURRENCE_THRESHOLD.
     */
    fun computeBestWorst(
        logs: List<DailyLog>,
        activities: List<BestActivityDto>
    ): Pair<List<ActivityCorrelation>, List<ActivityCorrelation>> {
        android.util.Log.d("InsightsEngine", "computeBestWorst: ${logs.size} logs, ${activities.size} activities")

        if (logs.isEmpty() || activities.isEmpty()) {
            android.util.Log.w("InsightsEngine", "computeBestWorst: empty input → no correlations")
            return Pair(emptyList(), emptyList())
        }

        // Build lookup: activityId → dto AND activityName(normalized) → dto (fallback)
        val activityById = activities.associateBy { it.activityId }
        val activityByName = activities.associateBy { normalizeActivityName(it.activityName) }

        val totalCount = mutableMapOf<String, Int>()
        val bestCount = mutableMapOf<String, Int>()
        val worstCount = mutableMapOf<String, Int>()
        val moodScoreSum = mutableMapOf<String, Double>()
        val actIdToDto = mutableMapOf<String, BestActivityDto>()

        for (log in logs) {
            val actIds = log.activityIds ?: continue
            val mood = log.baseMoodId
            for (rawId in actIds) {
                val inputId = rawId.trim()
                if (inputId.isBlank()) continue
                
                // Try to resolve DTO by ID first, then by normalized name
                val dto = activityById[inputId]
                    ?: activityByName[normalizeActivityName(inputId)]
                    ?: continue
                
                val actId = dto.activityId // Always use the canonical ID from backend
                actIdToDto[actId] = dto
                totalCount[actId] = (totalCount[actId] ?: 0) + 1
                moodScoreSum[actId] = (moodScoreSum[actId] ?: 0.0) + mood
                if (mood >= 4) bestCount[actId] = (bestCount[actId] ?: 0) + 1
                if (mood <= 2) worstCount[actId] = (worstCount[actId] ?: 0) + 1
            }
        }

        android.util.Log.d("InsightsEngine", "computeBestWorst: matched ${actIdToDto.size} distinct activities from logs")

        val correlations = mutableListOf<ActivityCorrelation>()
        val threshold = if (totalCount.values.any { it >= MIN_OCCURRENCE_THRESHOLD }) MIN_OCCURRENCE_THRESHOLD else 1

        for ((actId, total) in totalCount) {
            if (total < threshold) continue
            val dto = actIdToDto[actId] ?: continue
            val best = bestCount[actId] ?: 0
            val worst = worstCount[actId] ?: 0
            val avgScore = (moodScoreSum[actId] ?: 0.0) / total
            val bestRate = ((best.toFloat() / total) * 100).roundToInt()
            val worstRate = ((worst.toFloat() / total) * 100).roundToInt()
            android.util.Log.d("InsightsEngine", "  ${dto.activityName}: total=$total best=$bestRate% worst=$worstRate% avgScore=${"%.2f".format(avgScore)}")
            correlations.add(
                ActivityCorrelation(
                    activityId = actId,
                    activityName = dto.activityName,
                    occurrence = total,
                    bestRatePercent = bestRate,
                    worstRatePercent = worstRate,
                    averageMoodScore = avgScore
                )
            )
        }

        if (correlations.isEmpty()) {
            android.util.Log.w("InsightsEngine", "computeBestWorst: no correlations found even with threshold=$threshold → returning empty (will use legacy fallback)")
            return Pair(emptyList(), emptyList())
        }

        val bestList = correlations.sortedByDescending { it.bestRatePercent }.take(3)
        val worstList = correlations.sortedByDescending { it.worstRatePercent }.take(3)

        android.util.Log.d("InsightsEngine", "Best top3: ${bestList.map { "${it.activityName}(${it.bestRatePercent}%)" }}")
        android.util.Log.d("InsightsEngine", "Worst top3: ${worstList.map { "${it.activityName}(${it.worstRatePercent}%)" }}")

        return Pair(bestList, worstList)
    }

    /**
     * Compute full Icon Deep Dive for a specific activityId.
     */
    fun computeIconDeepDive(
        activityId: String?,
        logs: List<DailyLog>,
        activities: List<BestActivityDto>
    ): IconDeepDiveResult? {
        val allIds = activities.map { it.activityId }.toSet()

        // Find target DTO
        val targetDto = activityId?.let { id ->
            activities.find { it.activityId == id }
        } ?: activities.firstOrNull() ?: return null

        val targetId = targetDto.activityId

        // Filter logs that contain this activity (by ID or normalized name)
        val normalizedTargetName = normalizeActivityName(targetDto.activityName)
        val relevantLogs = logs.filter { log ->
            log.activityIds?.any { id -> 
                val cleanId = id.trim()
                cleanId == targetId || normalizeActivityName(cleanId) == normalizedTargetName
            } == true
        }

        if (relevantLogs.isEmpty()) {
            return IconDeepDiveResult(
                activityId = targetId,
                activityName = targetDto.activityName,
                totalOccurrence = targetDto.occurrence,
                averageMoodScore = targetDto.averageMoodScore,
                moodDistribution = emptyList(),
                longestStreak = 0,
                weeklyFrequency = 0.0,
                relatedActivities = emptyList()
            )
        }

        // --- Metric 1: Mood Distribution ---
        val moodDistribution = targetDto.moodDistribution?.map { dist ->
            MoodDistributionEntry(
                moodId = dist.baseMoodId ?: 3,
                count = dist.count,
                percentage = dist.percentage.toFloat()
            )
        }?.sortedBy { it.moodId } ?: run {
            val moodCounts = relevantLogs.groupBy { it.baseMoodId }.mapValues { it.value.size }
            val total = relevantLogs.size
            (1..5).map { moodId ->
                val count = moodCounts[moodId] ?: 0
                MoodDistributionEntry(
                    moodId = moodId,
                    count = count,
                    percentage = if (total > 0) (count.toFloat() / total) * 100f else 0f
                )
            }
        }

        // --- Metric 2: Average Mood Score ---
        val avgMoodScore = relevantLogs.map { it.baseMoodId.toDouble() }.average()

        // --- Metric 3: Frequency & Streaks ---
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val sortedDates = relevantLogs.mapNotNull { log ->
            try { LocalDate.parse(log.date, dateFormatter) } catch (e: Exception) { null }
        }.sorted()

        // Longest streak of consecutive days
        val longestStreak = if (sortedDates.isEmpty()) 0 else {
            var maxStreak = 1
            var currentStreak = 1
            for (i in 1 until sortedDates.size) {
                val diff = ChronoUnit.DAYS.between(sortedDates[i - 1], sortedDates[i])
                currentStreak = if (diff == 1L) currentStreak + 1 else 1
                if (currentStreak > maxStreak) maxStreak = currentStreak
            }
            maxStreak
        }

        // Weekly frequency: occurrence / number of weeks spanned
        val weeklyFreq = if (sortedDates.size >= 2) {
            val totalDays = ChronoUnit.DAYS.between(sortedDates.first(), sortedDates.last()).toDouble()
            val totalWeeks = (totalDays / 7.0).coerceAtLeast(1.0)
            relevantLogs.size / totalWeeks
        } else relevantLogs.size.toDouble()

        // --- Metric 4: Co-occurrence (Jaccard simplified) ---
        val coOccurrence = mutableMapOf<String, Int>()
        for (log in relevantLogs) {
            val otherIds = log.activityIds?.filter { it.trim() != targetId && it.isNotBlank() } ?: continue
            for (otherId in otherIds) {
                coOccurrence[otherId] = (coOccurrence[otherId] ?: 0) + 1
            }
        }
        // Map IDs to names for top 3
        val activityNameMap = activities.associateBy({ it.activityId }, { it.activityName })
        val relatedActivities = coOccurrence.entries
            .sortedByDescending { it.value }
            .take(3)
            .mapNotNull { (id, count) ->
                val name = activityNameMap[id] ?: return@mapNotNull null
                Pair(name, count)
            }

        return IconDeepDiveResult(
            activityId = targetId,
            activityName = targetDto.activityName,
            totalOccurrence = targetDto.occurrence, // Use real data from API
            averageMoodScore = targetDto.averageMoodScore, // Use real data from API
            moodDistribution = moodDistribution,
            longestStreak = longestStreak,
            weeklyFrequency = weeklyFreq,
            relatedActivities = relatedActivities
        )
    }

    private fun normalizeActivityName(name: String): String {
        return name.filterNot { it.isWhitespace() }.lowercase()
    }

    /**
     * Filter logs for a specific period (year/month).
     */
    fun filterLogsByPeriod(
        logs: List<DailyLog>,
        year: Int,
        month: Int?,
        isMonthly: Boolean
    ): List<DailyLog> {
        return logs.filter { log ->
            try {
                val date = LocalDate.parse(log.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                if (isMonthly && month != null) {
                    date.year == year && date.monthValue == month
                } else {
                    date.year == year
                }
            } catch (e: Exception) { false }
        }
    }
}
