package com.diary.moonpage.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diary.moonpage.data.local.MoonPageDatabase
import com.diary.moonpage.data.local.entity.CustomThemeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomThemeDaoTest {

    private lateinit var database: MoonPageDatabase
    private lateinit var customThemeDao: CustomThemeDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MoonPageDatabase::class.java
        ).allowMainThreadQueries().build()
        customThemeDao = database.customThemeDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetCustomTheme_returnsCorrectData() = runBlocking {
        val theme = CustomThemeEntity(
            id = "theme_1",
            name = "My Custom Theme",
            bgFilePath = "/path/to/bg",
            primaryColor = "#FFFFFF",
            iconColor = "#000000",
            iconColors = "#000000",
            lightConfigJson = "{}",
            darkConfigJson = "{}",
            createdAt = 123456789L
        )

        customThemeDao.upsert(theme)

        val themes = customThemeDao.observeCustomThemes().first()
        assertEquals(1, themes.size)
        assertEquals("theme_1", themes[0].id)
        assertEquals("My Custom Theme", themes[0].name)
    }

    @Test
    fun upsertCustomTheme_replacesExistingData() = runBlocking {
        val theme1 = CustomThemeEntity(
            id = "theme_1",
            name = "My Custom Theme",
            bgFilePath = "/path/to/bg",
            primaryColor = "#FFFFFF",
            iconColor = "#000000",
            iconColors = "#000000",
            lightConfigJson = "{}",
            darkConfigJson = "{}",
            createdAt = 123456789L
        )
        customThemeDao.upsert(theme1)

        val theme2 = theme1.copy(name = "Updated Theme Name")
        customThemeDao.upsert(theme2)

        val themes = customThemeDao.observeCustomThemes().first()
        assertEquals(1, themes.size)
        assertEquals("Updated Theme Name", themes[0].name)
    }
}
