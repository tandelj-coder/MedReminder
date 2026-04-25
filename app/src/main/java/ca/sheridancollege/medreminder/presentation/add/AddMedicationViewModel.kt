package ca.sheridancollege.medreminder.presentation.add

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.sheridancollege.medreminder.data.repository.DrugRepository
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.DrugSuggestion
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.domain.model.MedicationTime
import ca.sheridancollege.medreminder.domain.model.MedicationType
import ca.sheridancollege.medreminder.domain.usecase.AddMedicationUseCase
import ca.sheridancollege.medreminder.worker.DoseEventGenerationWorker
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
    val dosageAmount: String = "",
    val dosageUnit: String = "mg",
    val medicationType: MedicationType = MedicationType.TABLET,
    val times: List<MedicationTime> = listOf(MedicationTime(8, 0)),
    val selectedDays: List<DayOfWeek> = DayOfWeek.entries,
    val instructions: String = "",
    val notes: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val isAsNeeded: Boolean = false,
    val maxPerDay: String = "",
    
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
                    dosageAmount = med.dosageAmount.toString(),
                    dosageUnit = med.dosageUnit,
                    medicationType = med.medicationType,
                    times = med.times,
                    selectedDays = med.days,
                    instructions = med.instructions ?: "",
                    notes = med.notes,
                    startDate = med.startDate,
                    endDate = med.endDate,
                    isAsNeeded = med.isAsNeeded,
                    maxPerDay = med.maxPerDay?.toString() ?: "",
                    isEditMode = true
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
        triggerDrugSearch(value)
    }

    fun onDosageAmountChange(value: String) = _uiState.update { it.copy(dosageAmount = value) }
    fun onDosageUnitChange(value: String) = _uiState.update { it.copy(dosageUnit = value) }
    fun onMedicationTypeChange(type: MedicationType) = _uiState.update { it.copy(medicationType = type) }
    
    fun onAddTime() {
        _uiState.update { it.copy(times = it.times + MedicationTime(12, 0)) }
    }
    
    fun onRemoveTime(index: Int) {
        _uiState.update { 
            val newTimes = it.times.toMutableList()
            if (newTimes.size > 1) {
                newTimes.removeAt(index)
            }
            it.copy(times = newTimes)
        }
    }

    fun onTimeChange(index: Int, hour: Int, minute: Int) {
        _uiState.update { 
            val newTimes = it.times.toMutableList()
            newTimes[index] = MedicationTime(hour, minute)
            it.copy(times = newTimes)
        }
    }

    fun onInstructionsChange(value: String) = _uiState.update { it.copy(instructions = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }
    
    fun onStartDateChange(date: Long) = _uiState.update { it.copy(startDate = date) }
    fun onEndDateChange(date: Long?) = _uiState.update { it.copy(endDate = date) }

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
                dosageAmount = state.dosageAmount.toDoubleOrNull() ?: 0.0,
                dosageUnit = state.dosageUnit,
                medicationType = state.medicationType,
                times = state.times,
                days = state.selectedDays,
                startDate = state.startDate,
                endDate = state.endDate,
                instructions = state.instructions.trim().ifBlank { null },
                isAsNeeded = state.isAsNeeded,
                maxPerDay = state.maxPerDay.toIntOrNull(),
                notes = state.notes.trim()
            )
            
            if (state.isEditMode) {
                repository.updateMedication(medication)
            } else {
                repository.insertMedication(medication)
            }
            // Trigger generation of DoseEvents and Alarms immediately
            DoseEventGenerationWorker.scheduleOnAppStart(context)

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
