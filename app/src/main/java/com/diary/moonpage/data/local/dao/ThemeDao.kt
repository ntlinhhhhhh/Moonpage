package com.diary.moonpage.data.local.dao

import androidx.room.*
import com.diary.moonpage.data.local.entity.ThemeEntity
import com.diary.moonpage.data.local.entity.ThemeMoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeDao {
    @Query("SELECT * FROM themes")
    fun getAllThemes(): Flow<List<ThemeEntity>>

    @Query("SELECT * FROM themes WHERE isOwned = 1")
    fun getOwnedThemes(): Flow<List<ThemeEntity>>

    @Query("SELECT * FROM themes WHERE id = :id")
    suspend fun getThemeById(id: String): ThemeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThemes(themes: List<ThemeEntity>)

    @Query("UPDATE themes SET isActive = 0")
    suspend fun clearActiveTheme()

    @Query("UPDATE themes SET isActive = 1, activatedAt = :timestamp WHERE id = :id")
    suspend fun setActiveTheme(id: String, timestamp: Long)

    @Query("SELECT * FROM themes WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTheme(): com.diary.moonpage.data.local.entity.ThemeEntity?

    @Query("DELETE FROM themes")
    suspend fun deleteAllThemes()

    // Mood related queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThemeMoods(moods: List<ThemeMoodEntity>)

    @Query("SELECT * FROM theme_moods WHERE themeId = :themeId")
    suspend fun getMoodsForTheme(themeId: String): List<ThemeMoodEntity>

    @Query("DELETE FROM theme_moods")
    suspend fun deleteAllThemeMoods()
}
