package ca.sheridancollege.medreminder.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.DoseEvent
import ca.sheridancollege.medreminder.domain.model.IntakeLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HistoryUiState(
    val logs: List<IntakeLog> = emptyList(),
    val doseEvents: List<DoseEvent> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getAllLogs(),
        repository.getAllDoseEvents()
    ) { logs, events ->
        HistoryUiState(logs = logs, doseEvents = events, isLoading = false)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState()
        )
}