package com.diary.moonpage.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "onboarding_prefs")

@Singleton
class OnboardingPrefsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Lưu danh sách userId đã hoàn thành onboarding (cách nhau bằng dấu phẩy)
        private val COMPLETED_USERS_KEY = stringPreferencesKey("completed_user_ids")
    }

    /** Kiểm tra xem userId này đã hoàn thành onboarding chưa */
    suspend fun checkOnboardingCompleted(userId: String): Boolean {
        val completedIds = context.onboardingDataStore.data
            .map { prefs -> prefs[COMPLETED_USERS_KEY] ?: "" }
            .first()
        return userId in completedIds.split(",").filter { it.isNotBlank() }
    }

    /** Đánh dấu userId này đã hoàn thành onboarding */
    suspend fun setOnboardingCompleted(userId: String) {
        context.onboardingDataStore.edit { prefs ->
            val current = prefs[COMPLETED_USERS_KEY] ?: ""
            val set = current.split(",").filter { it.isNotBlank() }.toMutableSet()
            set.add(userId)
            prefs[COMPLETED_USERS_KEY] = set.joinToString(",")
        }
    }

    /** Reset onboarding cho userId cụ thể (dùng cho testing) */
    suspend fun resetOnboarding(userId: String) {
        context.onboardingDataStore.edit { prefs ->
            val current = prefs[COMPLETED_USERS_KEY] ?: ""
            val set = current.split(",").filter { it.isNotBlank() }.toMutableSet()
            set.remove(userId)
            prefs[COMPLETED_USERS_KEY] = set.joinToString(",")
        }
    }
}
