package ca.sheridancollege.medreminder.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.sheridancollege.medreminder.domain.model.AdherenceStats
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.domain.usecase.DeleteMedicationUseCase
import ca.sheridancollege.medreminder.domain.usecase.GetAdherenceStatsUseCase
import ca.sheridancollege.medreminder.domain.usecase.GetTodayMedicationsUseCase
import ca.sheridancollege.medreminder.domain.usecase.MarkAsTakenResult
import ca.sheridancollege.medreminder.domain.usecase.MarkAsTakenUseCase
import ca.sheridancollege.medreminder.domain.usecase.ResetDailyStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val medications: List<Medication> = emptyList(),
    val adherenceStats: AdherenceStats = AdherenceStats(0, 0, 0),
    val isLoading: Boolean = true,
    val doubleDoseWarning: DoubleDoseWarningState? = null,
    val snackbarMessage: String? = null,
    val showResetConfirm: Boolean = false
)

data class DoubleDoseWarningState(
    val medication: Medication,
    val lastTakenAt: Long,
    val minutesSinceLastDose: Long
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayMedications: GetTodayMedicationsUseCase,
    private val markAsTaken: MarkAsTakenUseCase,
    private val getAdherenceStats: GetAdherenceStatsUseCase,
    private val deleteMedication: DeleteMedicationUseCase,
    private val resetDailyStatus: ResetDailyStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadMedications()
        loadStats()
    }

    private fun loadMedications() {
        viewModelScope.launch {
            getTodayMedications()
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { meds ->
                    _uiState.update { it.copy(medications = meds, isLoading = false) }
                }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            getAdherenceStats()
                .collect { stats ->
                    _uiState.update { it.copy(adherenceStats = stats) }
                }
        }
    }

    fun onMarkAsTaken(medication: Medication, forceConfirm: Boolean = false) {
        viewModelScope.launch {
            when (val result = markAsTaken(medication, forceConfirm)) {
                is MarkAsTakenResult.Success -> {
                    _uiState.update {
                        it.copy(
                            doubleDoseWarning = null,
                            snackbarMessage = "${medication.name} marked as taken ✓"
                        )
                    }
                }
                is MarkAsTakenResult.DoubleDoseWarning -> {
                    _uiState.update {
                        it.copy(
                            doubleDoseWarning = DoubleDoseWarningState(
                                medication = result.medication,
                                lastTakenAt = result.lastTakenAt,
                                minutesSinceLastDose = result.minutesSinceLastDose
                            )
                        )
                    }
                }
                is MarkAsTakenResult.AlreadyTaken -> {
                    _uiState.update { it.copy(snackbarMessage = "Already taken today") }
                }
            }
        }
    }

    fun onDeleteMedication(medication: Medication) {
        viewModelScope.launch {
            deleteMedication(medication)
            _uiState.update { it.copy(snackbarMessage = "${medication.name} removed") }
        }
    }

    fun onResetTodayRequested() {
        _uiState.update { it.copy(showResetConfirm = true) }
    }

    fun onResetTodayConfirmed() {
        viewModelScope.launch {
            resetDailyStatus()
            _uiState.update {
                it.copy(
                    showResetConfirm = false,
                    snackbarMessage = "Today's status reset"
                )
            }
        }
    }

    fun onResetTodayDismissed() {
        _uiState.update { it.copy(showResetConfirm = false) }
    }

    fun onDismissDoubleDoseWarning() {
        _uiState.update { it.copy(doubleDoseWarning = null) }
    }

    fun onConfirmDoubleDose(medication: Medication) {
        onMarkAsTaken(medication, forceConfirm = true)
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
