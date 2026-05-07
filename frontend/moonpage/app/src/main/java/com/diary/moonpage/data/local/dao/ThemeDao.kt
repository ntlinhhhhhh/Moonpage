package com.diary.moonpage.data.local.dao

import androidx.room.*
import com.diary.moonpage.data.local.entity.ThemeEntity
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

    @Query("UPDATE themes SET isActive = 1 WHERE id = :id")
    suspend fun setActiveTheme(id: String)

    @Query("DELETE FROM themes")
    suspend fun deleteAllThemes()
}
