package com.diary.moonpage.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diary.moonpage.data.local.entity.CustomThemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomThemeDao {
    @Query("SELECT * FROM custom_themes ORDER BY createdAt DESC")
    fun observeCustomThemes(): Flow<List<CustomThemeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(theme: CustomThemeEntity)
}
