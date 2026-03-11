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
        val EMERGENCY_CONTACT_NAME = stringPreferencesKey("emergency_contact_name")
        val EMERGENCY_CONTACT_PHONE = stringPreferencesKey("emergency_contact_phone")
        val EMERGENCY_CONTACT_RELATION = stringPreferencesKey("emergency_contact_relation")
        val USER_MEDICAL_CONDITIONS = stringPreferencesKey("user_medical_conditions")
        val USER_BLOOD_TYPE = stringPreferencesKey("user_blood_type")
        val USER_ALLERGIES = stringPreferencesKey("user_allergies")
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

    val emergencyContactName: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[EMERGENCY_CONTACT_NAME] ?: "" }

    val emergencyContactPhone: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[EMERGENCY_CONTACT_PHONE] ?: "" }

    val emergencyContactRelation: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[EMERGENCY_CONTACT_RELATION] ?: "" }

    val userMedicalConditions: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[USER_MEDICAL_CONDITIONS] ?: "" }

    val userBloodType: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[USER_BLOOD_TYPE] ?: "" }

    val userAllergies: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[USER_ALLERGIES] ?: "" }

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

    suspend fun setEmergencyContact(name: String, phone: String, relation: String) {
        context.dataStore.edit {
            it[EMERGENCY_CONTACT_NAME] = name
            it[EMERGENCY_CONTACT_PHONE] = phone
            it[EMERGENCY_CONTACT_RELATION] = relation
        }
    }

    suspend fun setMedicalInfo(bloodType: String, conditions: String, allergies: String) {
        context.dataStore.edit {
            it[USER_BLOOD_TYPE] = bloodType
            it[USER_MEDICAL_CONDITIONS] = conditions
            it[USER_ALLERGIES] = allergies
        }
    }

    suspend fun resetStreak() {
        context.dataStore.edit { it[STREAK_COUNT] = 0 }
    }
}