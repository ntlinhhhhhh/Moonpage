package com.diary.moonpage.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.diary.moonpage.domain.model.Activity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

private val Context.activityDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "activity_prefs")

@Singleton
class ActivityPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ENABLED_CATEGORIES_KEY = stringPreferencesKey("enabled_categories")
        private val ACTIVITIES_JSON_KEY = stringPreferencesKey("activities_json")

        val DEFAULT_ENABLED = setOf("Hobbies", "Emotions", "Meals", "SelfCare")

        val ALL_CATEGORY_KEYS = listOf(
            "Hobbies", "Emotions", "Meals", "SelfCare", "Chores",
            "Events", "People", "Beauty", "Weather", "Health",
            "Work", "Other", "School", "Relationship"
        )
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val enabledCategories: StateFlow<Set<String>> = context.activityDataStore.data.map { prefs ->
        val saved = prefs[ENABLED_CATEGORIES_KEY]
        if (saved.isNullOrEmpty()) DEFAULT_ENABLED
        else saved.split(",").filter { it.isNotEmpty() }.toSet()
    }.stateIn(scope, SharingStarted.Eagerly, DEFAULT_ENABLED)

    suspend fun saveEnabledCategories(categories: Set<String>) {
        context.activityDataStore.edit { prefs ->
            prefs[ENABLED_CATEGORIES_KEY] = categories.joinToString(",")
        }
    }

    val activities: StateFlow<List<Activity>> = context.activityDataStore.data.map { prefs ->
        val json = prefs[ACTIVITIES_JSON_KEY] ?: DefaultActivities.json
        val type = object : TypeToken<List<Activity>>() {}.type
        try {
            Gson().fromJson(json, type) ?: emptyList<Activity>()
        } catch (e: Exception) {
            emptyList<Activity>()
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList<Activity>())

    suspend fun saveActivities(json: String) {
        context.activityDataStore.edit { prefs ->
            prefs[ACTIVITIES_JSON_KEY] = json
        }
    }
}
