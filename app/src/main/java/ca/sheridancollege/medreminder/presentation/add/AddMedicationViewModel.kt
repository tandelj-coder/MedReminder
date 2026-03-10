package ca.sheridancollege.medreminder.presentation.add

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.domain.usecase.AddMedicationUseCase
import ca.sheridancollege.medreminder.worker.MedicationReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val daysError: String? = null
)

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val addMedication: AddMedicationUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, nameError = null) }

    fun onDosageChange(value: String) =
        _uiState.update { it.copy(dosage = value) }

    fun onTimeChange(hour: Int, minute: Int) =
        _uiState.update { it.copy(timeHour = hour, timeMinute = minute) }

    fun onNotesChange(value: String) =
        _uiState.update { it.copy(notes = value) }

    fun onDayToggled(day: DayOfWeek) {
        val current = _uiState.value.selectedDays.toMutableList()
        if (current.contains(day)) current.remove(day) else current.add(day)
        _uiState.update { it.copy(selectedDays = current, daysError = null) }
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
                name = state.name.trim(),
                dosage = state.dosage.trim(),
                timeHour = state.timeHour,
                timeMinute = state.timeMinute,
                days = state.selectedDays,
                notes = state.notes.trim()
            )
            val id = addMedication(medication)
            MedicationReminderWorker.schedule(
                context = context,
                medicationId = id.toInt(),
                medicationName = medication.name,
                dosage = medication.dosage,
                hour = medication.timeHour,
                minute = medication.timeMinute
            )
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}