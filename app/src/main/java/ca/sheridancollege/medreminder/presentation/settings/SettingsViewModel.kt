package ca.sheridancollege.medreminder.presentation.settings

import ca.sheridancollege.medreminder.data.datastore.UserPreferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val snoozeMinutes: Int = 10,
    val darkTheme: Boolean = false,
    val streakCount: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        dataStore.notificationsEnabled,
        dataStore.snoozeMinutes,
        dataStore.darkTheme,
        dataStore.streakCount
    ) { notifications, snooze, dark, streak ->
        SettingsUiState(
            notificationsEnabled = notifications,
            snoozeMinutes = snooze,
            darkTheme = dark,
            streakCount = streak
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { dataStore.setNotificationsEnabled(enabled) }
    }

    fun setSnoozeMinutes(minutes: Int) {
        viewModelScope.launch { dataStore.setSnoozeMinutes(minutes) }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { dataStore.setDarkTheme(enabled) }
    }
}