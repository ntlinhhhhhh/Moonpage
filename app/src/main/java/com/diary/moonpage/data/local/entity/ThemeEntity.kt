package com.diary.moonpage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diary.moonpage.domain.model.Theme
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
    val icons: String, // Comma separated
    val primaryColor: String?,
    val decoration: String,
    val activatedAt: Long? = null
) {
    fun toDomain(): Theme {
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
            icons = if (icons.isEmpty()) emptyList() else icons.split(","),
            primaryColor = primaryColor,
            decoration = decoration,
            activatedAt = activatedAt
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
                icons = theme.icons.joinToString(","),
                primaryColor = theme.primaryColor,
                decoration = theme.decoration,
                activatedAt = theme.activatedAt
            )
        }
    }
}
