package com.diary.moonpage.widget.glance

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import coil.imageLoader
import coil.request.ImageRequest
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonBgDark
import com.diary.moonpage.core.theme.MoonBgLight
import com.diary.moonpage.core.theme.MoonTextDark
import com.diary.moonpage.core.theme.MoonTextDarkNew
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.core.theme.getThemeShades
import com.diary.moonpage.domain.model.Activity
import com.diary.moonpage.domain.model.DailyLog
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private const val BASE_URL = "https://hieu-wikipedia.io.vn/"

data class WidgetPalette(
    val daySurface: Color,
    val nightSurface: Color,
    val dayOnSurface: Color,
    val nightOnSurface: Color,
    val dayBadge: Color,
    val nightBadge: Color,
    val dayBadgeText: Color,
    val nightBadgeText: Color,
    val daySurfaceVariant: Color,
    val nightSurfaceVariant: Color,
    // Mood colors per level (1=very_sad, 2=sad, 3=neutral, 4=happy, 5=very_happy)
    val moodColors: List<Color>
)

data class WidgetDaySnapshot(
    val streakCount: Int,
    val moodResId: Int?,
    val moodLevel: Int,
    val note: String,
    val photoUris: List<String>,
    val footerItems: List<WidgetFooterItem>,
    val palette: WidgetPalette
)

data class WidgetFooterItem(
    val emoji: String,
    val label: String
)

data class WeekDayMood(
    val dayLabel: String,     // "Sun", "Mon", ...
    val dayNumber: Int,       // 1..31
    val moodResId: Int?,      // null = no log
    val moodColor: Color,
    val isToday: Boolean
)

data class MonthDayMood(
    val dayNumber: Int,       // 1..31 (0 = empty cell before month starts)
    val moodResId: Int?,
    val moodColor: Color,
    val isToday: Boolean,
    val isEmpty: Boolean      // padding cell before first day
)

class MoonpageWidgetDataSource(private val context: Context) {

    private val entryPoint: MoonpageWidgetEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            MoonpageWidgetEntryPoint::class.java
        )
    }

    suspend fun loadTodaySnapshot(): WidgetDaySnapshot = withContext(Dispatchers.IO) {
        val today = LocalDate.now().toString()
        val log = loadLogByDate(today)
        val user = entryPoint.userManager().getUser().firstOrNull()
        val themeType = entryPoint.themePreferencesManager().themeType.firstOrNull() ?: MoonThemeType.DEFAULT
        val activities = entryPoint.activityPreferencesManager().activities.firstOrNull().orEmpty()

        WidgetDaySnapshot(
            streakCount = user?.currentStreak ?: 0,
            moodResId = log?.baseMoodId?.let(::mapMoodDrawable),
            moodLevel = log?.baseMoodId ?: 0,
            note = log?.note?.trim().orEmpty(),
            photoUris = log?.dailyPhotos.orEmpty().map(::normalizePhotoPath),
            footerItems = buildFooterItems(log, activities),
            palette = resolvePalette(themeType)
        )
    }

    /**
     * Load mood data for the current week (Sun → Sat containing today).
     */
    suspend fun loadWeekSnapshot(): List<WeekDayMood> = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val themeType = entryPoint.themePreferencesManager().themeType.firstOrNull() ?: MoonThemeType.DEFAULT
        val shades = getThemeShades(themeType)
        val emptyColor = Color(0x22888888)

        // Find the Sunday of the current week
        val dayOfWeek = today.dayOfWeek
        val daysFromSunday = if (dayOfWeek == DayOfWeek.SUNDAY) 0 else dayOfWeek.value
        val weekStart = today.minusDays(daysFromSunday.toLong())

        (0..6).map { offset ->
            val day = weekStart.plusDays(offset.toLong())
            val log = loadLogByDate(day.toString())
            val moodLevel = log?.baseMoodId ?: 0
            WeekDayMood(
                dayLabel = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                dayNumber = day.dayOfMonth,
                moodResId = if (moodLevel > 0) mapMoodDrawable(moodLevel) else null,
                moodColor = if (moodLevel > 0) shades[(5 - moodLevel).coerceIn(0, 4)] else emptyColor,
                isToday = day == today
            )
        }
    }

    /**
     * Load mood data for the current month grid (padded from Sunday of first week).
     */
    suspend fun loadMonthSnapshot(): List<MonthDayMood> = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val yearMonth = YearMonth.of(today.year, today.month)
        val firstDay = yearMonth.atDay(1)
        val themeType = entryPoint.themePreferencesManager().themeType.firstOrNull() ?: MoonThemeType.DEFAULT
        val shades = getThemeShades(themeType)
        val emptyColor = Color(0x22888888)

        // Padding: how many empty cells before day 1 (0=Sun, 1=Mon, ...)
        val paddingCount = if (firstDay.dayOfWeek == DayOfWeek.SUNDAY) 0 else firstDay.dayOfWeek.value

        val result = mutableListOf<MonthDayMood>()
        // Empty padding cells
        repeat(paddingCount) {
            result.add(MonthDayMood(0, null, Color.Transparent, false, true))
        }
        // Actual days
        for (dayNum in 1..yearMonth.lengthOfMonth()) {
            val date = yearMonth.atDay(dayNum)
            val log = loadLogByDate(date.toString())
            val moodLevel = log?.baseMoodId ?: 0
            result.add(
                MonthDayMood(
                    dayNumber = dayNum,
                    moodResId = if (moodLevel > 0) mapMoodDrawable(moodLevel) else null,
                    moodColor = if (moodLevel > 0) shades[(5 - moodLevel).coerceIn(0, 4)] else emptyColor,
                    isToday = date == today,
                    isEmpty = false
                )
            )
        }
        result
    }

    fun isNightMode(): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    fun getWidgetPreferences() = entryPoint.widgetPreferencesManager()

    suspend fun loadBitmap(model: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (model.isNullOrBlank()) return@withContext null
        val request = ImageRequest.Builder(context)
            .data(model)
            .allowHardware(false)
            .size(1024, 1024)
            .build()
        val result = context.imageLoader.execute(request).drawable ?: return@withContext null
        if (result is BitmapDrawable) return@withContext result.bitmap
        val width = result.intrinsicWidth.takeIf { it > 0 } ?: 1024
        val height = result.intrinsicHeight.takeIf { it > 0 } ?: 1024
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        result.setBounds(0, 0, canvas.width, canvas.height)
        result.draw(canvas)
        bitmap
    }

    private suspend fun loadLogByDate(date: String): DailyLog? {
        return entryPoint.dailyLogRepository().getDailyLogByDate(date).getOrNull()
            ?: entryPoint.dailyLogRepository().getDailyLogByDateFlow(date).firstOrNull()
    }

    private fun buildFooterItems(log: DailyLog?, activities: List<Activity>): List<WidgetFooterItem> {
        if (log == null) return emptyList()
        val items = mutableListOf<WidgetFooterItem>()

        // Mood
        log.baseMoodId.takeIf { it > 0 }?.let { level ->
            val emoji = when (level) {
                5 -> "😄"; 4 -> "🙂"; 3 -> "😐"; 2 -> "😞"; else -> "😢"
            }
            items += WidgetFooterItem(emoji, "")
        }
        // Sleep
        log.sleepHours?.takeIf { it > 0 }?.let {
            items += WidgetFooterItem("🌙", String.format(Locale.ENGLISH, "%.1fh", it))
        }
        // Steps
        log.steps?.takeIf { it > 0 }?.let {
            items += WidgetFooterItem("👟", String.format(Locale.ENGLISH, "%,d", it))
        }
        // Calories
        log.calories?.takeIf { it > 0 }?.let {
            items += WidgetFooterItem("🔥", "${it}kcal")
        }
        // Distance
        log.distance?.takeIf { it > 0 }?.let {
            items += WidgetFooterItem("📍", String.format(Locale.ENGLISH, "%.1fkm", it / 1000.0))
        }
        // Activities (top 3)
        log.activityIds.orEmpty()
            .mapNotNull { id -> activities.firstOrNull { it.id == id }?.name }
            .distinct()
            .take(3)
            .forEach { name -> items += WidgetFooterItem("•", name) }

        return items.take(6)
    }

    private fun normalizePhotoPath(path: String): String {
        return when {
            path.startsWith("http") -> path
            path.startsWith("content://") -> path
            path.startsWith("file://") -> path
            path.startsWith("/") -> "file://$path"
            else -> BASE_URL + path.trimStart('/')
        }
    }

    fun mapMoodDrawable(level: Int): Int {
        return when (level.coerceIn(1, 5)) {
            1 -> R.drawable.very_sad
            2 -> R.drawable.sad
            3 -> R.drawable.neutral
            4 -> R.drawable.happy
            else -> R.drawable.very_happy
        }
    }

    fun resolvePalette(themeType: MoonThemeType): WidgetPalette {
        val shades = getThemeShades(themeType)
        val dayBase = shades.first().copy(alpha = 0.22f).compositeOver(MoonBgLight)
        val nightBase = shades.last().copy(alpha = 0.28f).compositeOver(MoonBgDark)
        val daySurfaceVariant = shades.first().copy(alpha = 0.35f).compositeOver(MoonBgLight)
        val nightSurfaceVariant = shades.last().copy(alpha = 0.40f).compositeOver(MoonBgDark)
        val dayBadge = Color(0xAA111111)
        val nightBadge = Color(0xAAFFFFFF)
        return WidgetPalette(
            daySurface = dayBase,
            nightSurface = nightBase,
            dayOnSurface = MoonTextDark,
            nightOnSurface = MoonTextDarkNew,
            dayBadge = dayBadge,
            nightBadge = nightBadge,
            dayBadgeText = Color.White,
            nightBadgeText = Color(0xFF171717),
            daySurfaceVariant = daySurfaceVariant,
            nightSurfaceVariant = nightSurfaceVariant,
            // mood colors: index 0=level1(very_sad) ... index4=level5(very_happy)
            moodColors = listOf(shades[4], shades[3], shades[2], shades[1], shades[0])
        )
    }
}
