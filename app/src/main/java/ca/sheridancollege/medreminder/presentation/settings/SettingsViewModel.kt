package ca.sheridancollege.medreminder.presentation.settings

import ca.sheridancollege.medreminder.data.datastore.UserPreferencesDataStore
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
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
    val streakCount: Int = 0,
    val showResetConfirm: Boolean = false,
    val showDeleteAccountConfirm: Boolean = false,
    val resetDone: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
    private val repository: MedicationRepository
) : ViewModel() {

    private val _extra = MutableStateFlow(Triple(false, false, false)) // showConfirm, resetDone, showDeleteConfirm

    val uiState: StateFlow<SettingsUiState> = combine(
        dataStore.notificationsEnabled,
        dataStore.snoozeMinutes,
        dataStore.darkTheme,
        dataStore.streakCount,
        _extra
    ) { notifications, snooze, dark, streak, extra ->
        SettingsUiState(
            notificationsEnabled = notifications,
            snoozeMinutes = snooze,
            darkTheme = dark,
            streakCount = streak,
            showResetConfirm = extra.first,
            resetDone = extra.second,
            showDeleteAccountConfirm = extra.third
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
        viewModelScope.launch { dataStore.setSnoozeMinutes(minutes.coerceIn(5, 60)) }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { dataStore.setDarkTheme(enabled) }
    }

    // ── Reset All Data ──────────────────────────────────────────────
    fun onResetAllRequested() { _extra.value = Triple(true, false, false) }
    fun onResetAllDismissed() { _extra.value = Triple(false, false, false) }

    fun onResetAllConfirmed() {
        viewModelScope.launch {
            val allMeds = repository.getAllActiveMedications().first()
            allMeds.forEach { repository.deleteMedication(it) }
            dataStore.resetStreak()
            _extra.value = Triple(false, true, false)
        }
    }

    fun onResetDoneDismissed() { _extra.value = Triple(false, false, false) }

    // ── Delete Account ──────────────────────────────────────────────
    fun onDeleteAccountRequested() { _extra.value = Triple(false, false, true) }
    fun onDeleteAccountDismissed() { _extra.value = Triple(false, false, false) }
}
