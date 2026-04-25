package ca.sheridancollege.medreminder.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.sheridancollege.medreminder.data.repository.DoseEventRepository
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.AdherenceStats
import ca.sheridancollege.medreminder.domain.model.DoseEvent
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.domain.usecase.DeleteMedicationUseCase
import ca.sheridancollege.medreminder.domain.usecase.GetAdherenceStatsUseCase
import ca.sheridancollege.medreminder.domain.usecase.ResetDailyStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class TodayUiState(
    val doseEvents: List<DoseEvent> = emptyList(),
    val adherenceStats: AdherenceStats = AdherenceStats(0, 0, 0),
    val isLoading: Boolean = true,
    val doubleDoseWarning: DoubleDoseWarningState? = null,
    val snackbarMessage: String? = null,
    val showResetConfirm: Boolean = false,
    val nextDoseInfo: NextDoseInfo? = null
)

data class NextDoseInfo(
    val medicationName: String,
    val remainingTimeMillis: Long,
    val doseEventId: Int
)

data class DoubleDoseWarningState(
    val doseEvent: DoseEvent,
    val lastTakenAt: Long,
    val minutesSinceLastDose: Long
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val doseEventRepository: DoseEventRepository,
    private val medicationRepository: MedicationRepository,
    private val getAdherenceStats: GetAdherenceStatsUseCase,
    private val deleteMedication: DeleteMedicationUseCase,
    private val resetDailyStatus: ResetDailyStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadDoseEvents()
        loadStats()
        startCountdownTimer()
    }

    private fun loadDoseEvents() {
        viewModelScope.launch {
            val (startOfDay, endOfDay) = todayRange()
            doseEventRepository.getDoseEventsForDay(startOfDay, endOfDay)
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { doses ->
                    _uiState.update { it.copy(doseEvents = doses, isLoading = false) }
                    updateNextDose(doses)
                }
        }
    }

    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                updateNextDose(_uiState.value.doseEvents)
            }
        }
    }

    private fun updateNextDose(doses: List<DoseEvent>) {
        val now = System.currentTimeMillis()
        val nextDose = doses
            .filter { it.isScheduled() }
            .minByOrNull { it.scheduledTime }
            ?.takeIf { it.scheduledTime > now }

        _uiState.update {
            it.copy(
                nextDoseInfo = nextDose?.let { dose ->
                    NextDoseInfo(
                        dose.medicationName,
                        dose.scheduledTime - now,
                        dose.id
                    )
                }
            )
        }
    }

    private fun todayRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return start to (start + 24 * 60 * 60 * 1000L - 1)
    }

    private fun loadStats() {
        viewModelScope.launch {
            getAdherenceStats()
                .collect { stats ->
                    _uiState.update { it.copy(adherenceStats = stats) }
                }
        }
    }

    fun onMarkAsTaken(doseEvent: DoseEvent, forceConfirm: Boolean = false) {
        viewModelScope.launch {
            if (doseEvent.isTaken() && !forceConfirm) {
                _uiState.update { it.copy(snackbarMessage = "Already taken") }
                return@launch
            }

            val lastLog = medicationRepository.getLastIntakeForMedication(doseEvent.medicationId)
            if (lastLog != null && !forceConfirm) {
                val minutesSince = (System.currentTimeMillis() - lastLog.takenAt) / 60000
                if (minutesSince < 120) {
                    _uiState.update {
                        it.copy(
                            doubleDoseWarning = DoubleDoseWarningState(
                                doseEvent = doseEvent,
                                lastTakenAt = lastLog.takenAt,
                                minutesSinceLastDose = minutesSince
                            )
                        )
                    }
                    return@launch
                }
            }

            val result = doseEventRepository.markDoseAsTaken(doseEvent.id, System.currentTimeMillis())
            when {
                result.isSuccess -> {
                    _uiState.update {
                        it.copy(
                            doubleDoseWarning = null,
                            snackbarMessage = "${doseEvent.medicationName} marked as taken ✓"
                        )
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(snackbarMessage = "Failed to mark as taken")
                    }
                }
            }
        }
    }

    fun onDeleteMedication(doseEvent: DoseEvent) {
        viewModelScope.launch {
            val medication = medicationRepository.getMedicationById(doseEvent.medicationId) ?: return@launch
            deleteMedication(medication)
            _uiState.update { it.copy(snackbarMessage = "${doseEvent.medicationName} removed") }
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

    fun onConfirmDoubleDose(doseEvent: DoseEvent) {
        onMarkAsTaken(doseEvent, forceConfirm = true)
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
