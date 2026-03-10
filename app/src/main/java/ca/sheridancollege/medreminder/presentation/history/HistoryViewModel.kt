package ca.sheridancollege.medreminder.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.IntakeLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HistoryUiState(
    val logs: List<IntakeLog> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = repository.getAllLogs()
        .map { HistoryUiState(logs = it, isLoading = false) }
        .catch { emit(HistoryUiState(isLoading = false)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState()
        )
}