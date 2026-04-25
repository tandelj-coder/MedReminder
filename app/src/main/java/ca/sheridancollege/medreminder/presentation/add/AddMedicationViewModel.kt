package ca.sheridancollege.medreminder.presentation.add

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.sheridancollege.medreminder.data.repository.DrugRepository
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.DrugSuggestion
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.domain.usecase.AddMedicationUseCase
import ca.sheridancollege.medreminder.worker.MedicationAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMedicationUiState(
    val name: String = "",
    val dosage: String = "",
    val timeHour: Int = 8,
    val timeMinute: Int = 0,
    val selectedDays: List<DayOfWeek> = emptyList(),
    val notes: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: String? = null,
    val daysError: String? = null,
    val isEditMode: Boolean = false,
    // Drug search
    val drugSuggestions: List<DrugSuggestion> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val showSuggestions: Boolean = false
)

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val addMedication: AddMedicationUseCase,
    private val repository: MedicationRepository,
    private val drugRepository: DrugRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()
    private var editingId: Int = 0
    private var searchJob: Job? = null

    fun loadMedication(id: Int) {
        if (id == 0) return
        editingId = id
        viewModelScope.launch {
            val med = repository.getMedicationById(id) ?: return@launch
            _uiState.update {
                it.copy(
                    name = med.name,
                    dosage = med.dosage,
                    timeHour = med.timeHour,
                    timeMinute = med.timeMinute,
                    selectedDays = med.days,
                    notes = med.notes,
                    isEditMode = true
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
        triggerDrugSearch(value)
    }

    fun onDosageChange(value: String) = _uiState.update { it.copy(dosage = value) }
    fun onTimeChange(hour: Int, minute: Int) = _uiState.update { it.copy(timeHour = hour, timeMinute = minute) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun onDayToggled(day: DayOfWeek) {
        val current = _uiState.value.selectedDays.toMutableList()
        if (current.contains(day)) current.remove(day) else current.add(day)
        _uiState.update { it.copy(selectedDays = current, daysError = null) }
    }

    fun onDrugSelected(suggestion: DrugSuggestion) {
        _uiState.update {
            val genericNote = if (suggestion.genericName.isNotBlank() &&
                suggestion.genericName.lowercase() != suggestion.name.lowercase()
            ) suggestion.genericName else it.notes
            it.copy(
                name = suggestion.name,
                notes = genericNote,
                showSuggestions = false,
                drugSuggestions = emptyList(),
                searchError = null
            )
        }
    }

    fun onDismissSuggestions() {
        _uiState.update { it.copy(showSuggestions = false) }
    }

    fun onSave() {
        val state = _uiState.value
        var hasError = false
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Medication name is required") }
            hasError = true
        }
        if (state.selectedDays.isEmpty()) {
            _uiState.update { it.copy(daysError = "Select at least one day") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val medication = Medication(
                id = editingId,
                name = state.name.trim(),
                dosage = state.dosage.trim(),
                timeHour = state.timeHour,
                timeMinute = state.timeMinute,
                days = state.selectedDays,
                notes = state.notes.trim()
            )
            if (state.isEditMode) {
                repository.updateMedication(medication)
            } else {
                val id = addMedication(medication)
                MedicationAlarmScheduler.schedule(
                    context = context,
                    medicationId = id.toInt(),
                    medicationName = medication.name,
                    dosage = medication.dosage,
                    hour = medication.timeHour,
                    minute = medication.timeMinute
                )
            }
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    private fun triggerDrugSearch(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _uiState.update {
                it.copy(drugSuggestions = emptyList(), showSuggestions = false, searchError = null, isSearching = false)
            }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(isSearching = true, searchError = null) }
            drugRepository.searchDrugs(query).fold(
                onSuccess = { suggestions ->
                    _uiState.update {
                        it.copy(
                            drugSuggestions = suggestions,
                            isSearching = false,
                            showSuggestions = true,
                            searchError = null
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            drugSuggestions = emptyList(),
                            isSearching = false,
                            showSuggestions = false,
                            searchError = "Connection error. Enter name manually."
                        )
                    }
                }
            )
        }
    }
}
