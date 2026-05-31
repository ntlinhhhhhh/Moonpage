package com.diary.moonpage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeMood
import com.diary.moonpage.domain.model.ThemeType

@Entity(tableName = "themes")
data class ThemeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val collection: String,
    val price: Int,
    val isFree: Boolean,
    val thumbnailUrl: String?,
    val backgroundUrl: String?,
    val isOwned: Boolean,
    val isActive: Boolean,
    val description: String?,
    val type: String,
    val icons: String, // Comma separated — kept for DB schema compatibility
    val primaryColor: String?,
    val decoration: String,
    val activatedAt: Long? = null
) {
    fun toDomain(): Theme {
        val moodList = if (icons.isNotBlank()) {
            icons.split(",").mapIndexed { index, iconName ->
                ThemeMood(
                    baseMoodId = (5 - index).coerceIn(1, 5).toLong(),
                    customName = when (iconName.uppercase().trim()) {
                        "VERY_HAPPY" -> "Very Happy"
                        "HAPPY" -> "Happy"
                        "NEUTRAL" -> "Neutral"
                        "SAD" -> "Sad"
                        "VERY_SAD", "ANGRY" -> "Very Sad"
                        else -> "Neutral"
                    },
                    iconColor = primaryColor ?: "#FF8D6E63"
                )
            }
        } else {
            emptyList()
        }

        return Theme(
            id = id,
            name = name,
            collection = collection,
            price = price,
            isFree = isFree,
            thumbnailUrl = thumbnailUrl,
            backgroundUrl = backgroundUrl,
            isOwned = isOwned,
            isActive = isActive,
            description = description,
            type = ThemeType.valueOf(type),
            moods = moodList,
            primaryColor = primaryColor,
            decoration = decoration,
            activatedAt = activatedAt,
            isOfficial = !id.startsWith("custom_") && collection != "Custom Theme"
        )
    }

    companion object {
        fun fromDomain(theme: Theme): ThemeEntity {
            return ThemeEntity(
                id = theme.id,
                name = theme.name,
                collection = theme.collection,
                price = theme.price,
                isFree = theme.isFree,
                thumbnailUrl = theme.thumbnailUrl,
                backgroundUrl = theme.backgroundUrl,
                isOwned = theme.isOwned,
                isActive = theme.isActive,
                description = theme.description,
                type = theme.type.name,
                icons = theme.moods
                    .sortedByDescending { it.baseMoodId }
                    .joinToString(",") { it.customName.toEmotionString() }
                    .ifEmpty { "VERY_HAPPY,HAPPY,NEUTRAL,SAD,ANGRY" },
                primaryColor = theme.primaryColor,
                decoration = theme.decoration,
                activatedAt = theme.activatedAt
            )
        }
    }
}

// Helper to convert mood customName to legacy emotion string for icons column
private fun String.toEmotionString(): String {
    return when (this.lowercase()) {
        "very happy" -> "VERY_HAPPY"
        "happy" -> "HAPPY"
        "neutral" -> "NEUTRAL"
        "sad" -> "SAD"
        "very sad", "angry" -> "VERY_SAD"
        else -> "NEUTRAL"
    }
}
