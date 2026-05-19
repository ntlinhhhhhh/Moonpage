package com.diary.moonpage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_themes")
data class CustomThemeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bgFilePath: String,
    val primaryColor: String,
    val iconColor: String,
    val iconColors: String,
    val createdAt: Long
)
