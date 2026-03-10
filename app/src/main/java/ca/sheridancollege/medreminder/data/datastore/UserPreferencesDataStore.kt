package ca.sheridancollege.medreminder.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SNOOZE_MINUTES = intPreferencesKey("snooze_minutes")
        val RESET_HOUR = intPreferencesKey("reset_hour")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val STREAK_COUNT = intPreferencesKey("streak_count")
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[NOTIFICATIONS_ENABLED] ?: true }

    val snoozeMinutes: Flow<Int> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[SNOOZE_MINUTES] ?: 10 }

    val resetHour: Flow<Int> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[RESET_HOUR] ?: 0 }

    val darkTheme: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[DARK_THEME] ?: false }

    val streakCount: Flow<Int> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[STREAK_COUNT] ?: 0 }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setSnoozeMinutes(minutes: Int) {
        context.dataStore.edit { it[SNOOZE_MINUTES] = minutes }
    }

    suspend fun setResetHour(hour: Int) {
        context.dataStore.edit { it[RESET_HOUR] = hour }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[DARK_THEME] = enabled }
    }

    suspend fun incrementStreak() {
        context.dataStore.edit { it[STREAK_COUNT] = (it[STREAK_COUNT] ?: 0) + 1 }
    }

    suspend fun resetStreak() {
        context.dataStore.edit { it[STREAK_COUNT] = 0 }
    }
}