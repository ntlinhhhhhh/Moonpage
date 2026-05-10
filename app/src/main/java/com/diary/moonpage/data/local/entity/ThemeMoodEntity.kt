package com.diary.moonpage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "theme_moods",
    primaryKeys = ["themeId", "baseMoodId"],
    foreignKeys = [
        ForeignKey(
            entity = ThemeEntity::class,
            parentColumns = ["id"],
            childColumns = ["themeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["themeId"])]
)
data class ThemeMoodEntity(
    val themeId: String,
    val baseMoodId: String, // Awful, Bad, Meh, Good, Rad
    val iconUrl: String,
    val customName: String
)
