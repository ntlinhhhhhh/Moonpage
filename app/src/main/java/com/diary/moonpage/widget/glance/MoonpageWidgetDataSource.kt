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
import java.time.LocalDate
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
    val nightBadgeText: Color
)

data class WidgetDaySnapshot(
    val streakCount: Int,
    val moodResId: Int?,
    val note: String,
    val photoUris: List<String>,
    val footerItems: List<String>,
    val palette: WidgetPalette
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
        val log = loadTodayLog(today)
        val user = entryPoint.userManager().getUser().firstOrNull()
        val themeType = entryPoint.themePreferencesManager().themeType.firstOrNull() ?: MoonThemeType.DEFAULT
        val activities = entryPoint.activityPreferencesManager().activities.firstOrNull().orEmpty()

        WidgetDaySnapshot(
            streakCount = user?.currentStreak ?: 0,
            moodResId = log?.baseMoodId?.let(::mapMoodDrawable),
            note = log?.note?.trim().orEmpty(),
            photoUris = log?.dailyPhotos.orEmpty().map(::normalizePhotoPath),
            footerItems = buildFooterItems(log, activities),
            palette = resolvePalette(themeType)
        )
    }

    fun isNightMode(): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    fun getWidgetPreferences() = entryPoint.widgetPreferencesManager()

    suspend fun loadBitmap(model: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (model.isNullOrBlank()) {
            return@withContext null
        }

        val request = ImageRequest.Builder(context)
            .data(model)
            .allowHardware(false)
            .size(1024, 1024)
            .build()
        val result = context.imageLoader.execute(request).drawable ?: return@withContext null
        if (result is BitmapDrawable) {
            return@withContext result.bitmap
        }

        val width = result.intrinsicWidth.takeIf { it > 0 } ?: 1024
        val height = result.intrinsicHeight.takeIf { it > 0 } ?: 1024
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        result.setBounds(0, 0, canvas.width, canvas.height)
        result.draw(canvas)
        bitmap
    }

    private suspend fun loadTodayLog(date: String): DailyLog? {
        return entryPoint.dailyLogRepository().getDailyLogByDate(date).getOrNull()
            ?: entryPoint.dailyLogRepository().getDailyLogByDateFlow(date).firstOrNull()
    }

    private fun buildFooterItems(log: DailyLog?, activities: List<Activity>): List<String> {
        if (log == null) return emptyList()

        val items = mutableListOf<String>()
        log.sleepHours?.takeIf { it > 0 }?.let {
            items += String.format(Locale.ENGLISH, "%.1fh", it)
        }
        log.steps?.takeIf { it > 0 }?.let {
            items += String.format(Locale.ENGLISH, "%,d", it)
        }
        log.activityIds.orEmpty()
            .mapNotNull { activityId -> activities.firstOrNull { it.id == activityId }?.name }
            .distinct()
            .take(2)
            .forEach(items::add)
        return items.take(4)
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

    private fun mapMoodDrawable(level: Int): Int {
        return when (level.coerceIn(1, 5)) {
            1 -> R.drawable.very_sad
            2 -> R.drawable.sad
            3 -> R.drawable.neutral
            4 -> R.drawable.happy
            else -> R.drawable.very_happy
        }
    }

    private fun resolvePalette(themeType: MoonThemeType): WidgetPalette {
        val shades = getThemeShades(themeType)
        val dayBase = shades.first().copy(alpha = 0.22f).compositeOver(MoonBgLight)
        val nightBase = shades.last().copy(alpha = 0.28f).compositeOver(MoonBgDark)
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
            nightBadgeText = Color(0xFF171717)
        )
    }
}
