package ca.sheridancollege.medreminder.presentation.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.Medication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicationsUiState(
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null
)

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationsUiState())
    val uiState: StateFlow<MedicationsUiState> = _uiState.asStateFlow()

    init {
        loadMedications()
    }

    private fun loadMedications() {
        viewModelScope.launch {
            medicationRepository.getAllActiveMedications()
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { meds ->
                    _uiState.update { it.copy(medications = meds, isLoading = false) }
                }
        }
    }

    fun onDeleteMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medication)
            _uiState.update { it.copy(snackbarMessage = "${medication.name} removed") }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
