package ca.sheridancollege.medreminder.presentation.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Medication") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Medication Name *") },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Dosage
            OutlinedTextField(
                value = uiState.dosage,
                onValueChange = viewModel::onDosageChange,
                label = { Text("Dosage (e.g. 500mg)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Time picker
            Text("Reminder Time", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.timeHour.toString(),
                    onValueChange = {
                        val h = it.toIntOrNull()?.coerceIn(0, 23) ?: return@OutlinedTextField
                        viewModel.onTimeChange(h, uiState.timeMinute)
                    },
                    label = { Text("Hour (0-23)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.timeMinute.toString(),
                    onValueChange = {
                        val m = it.toIntOrNull()?.coerceIn(0, 59) ?: return@OutlinedTextField
                        viewModel.onTimeChange(uiState.timeHour, m)
                    },
                    label = { Text("Minute") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Text(
                "Scheduled: ${uiState.timeHour.let { if (it == 0) 12 else if (it > 12) it - 12 else it }}:" +
                        "${uiState.timeMinute.toString().padStart(2, '0')} " +
                        "${if (uiState.timeHour < 12) "AM" else "PM"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Day selector
            Text("Repeat On *", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DayOfWeek.values().forEach { day ->
                    FilterChip(
                        selected = uiState.selectedDays.contains(day),
                        onClick = { viewModel.onDayToggled(day) },
                        label = { Text(day.displayName.take(2)) }
                    )
                }
            }
            uiState.daysError?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            // Notes
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Save button
            Button(
                onClick = viewModel::onSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Medication", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}