package ca.sheridancollege.medreminder.presentation.add

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.DrugSuggestion
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditMode) "Edit Medication" else "Add Medication",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Drug name with autocomplete
            DrugAutocompleteField(
                value                = uiState.name,
                onValueChange        = viewModel::onNameChange,
                suggestions          = uiState.drugSuggestions,
                isSearching          = uiState.isSearching,
                showSuggestions      = uiState.showSuggestions,
                searchError          = uiState.searchError,
                onSuggestionSelected = viewModel::onDrugSelected,
                onDismiss            = viewModel::onDismissSuggestions,
                error                = uiState.nameError
            )

            MedTextField(
                value         = uiState.dosageAmount,
                onValueChange = viewModel::onDosageAmountChange,
                label         = "Dosage",
                placeholder   = "e.g. 500 mg"
            )

            // Time selector for first dose time
            val firstTime = uiState.times.firstOrNull()
            MedSelector(
                label   = "Reminder time",
                value   = String.format(
                    Locale.getDefault(), "%02d:%02d",
                    firstTime?.hour ?: 8, firstTime?.minute ?: 0
                ),
                icon    = Icons.Outlined.Schedule,
                onClick = {
                    TimePickerDialog(context, { _, h, m ->
                        viewModel.onTimeChange(0, h, m)
                    }, firstTime?.hour ?: 8, firstTime?.minute ?: 0, false).show()
                }
            )

            // Day selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Frequency",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    DayOfWeek.entries.forEach { day ->
                        val selected = uiState.selectedDays.contains(day)
                        FilterChip(
                            selected = selected,
                            onClick  = { viewModel.onDayToggled(day) },
                            label    = { Text(day.displayName) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                if (uiState.daysError != null) {
                    Text(
                        uiState.daysError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            MedTextField(
                value         = uiState.stockQuantity,
                onValueChange = viewModel::onStockQuantityChange,
                label         = "Pill count (optional)",
                placeholder   = "e.g. 30",
                keyboardType  = androidx.compose.ui.text.input.KeyboardType.Number
            )

            MedTextField(
                value         = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label         = "Notes (optional)",
                placeholder   = "Generic name, instructions…",
                singleLine    = false
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick  = viewModel::onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape   = RoundedCornerShape(12.dp),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (uiState.isEditMode) "Update Medication" else "Save Medication",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Shared field components ────────────────────────────────────────────────

@Composable
fun DrugAutocompleteField(
    value                : String,
    onValueChange        : (String) -> Unit,
    suggestions          : List<DrugSuggestion>,
    isSearching          : Boolean,
    showSuggestions      : Boolean,
    searchError          : String?,
    onSuggestionSelected : (DrugSuggestion) -> Unit,
    onDismiss            : () -> Unit,
    error                : String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "Medication name",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box {
            OutlinedTextField(
                value         = value,
                onValueChange = onValueChange,
                placeholder   = { Text("Search drug name…") },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                singleLine    = true,
                isError       = error != null,
                trailingIcon  = {
                    if (isSearching) CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                }
            )

            if (showSuggestions) {
                DropdownMenu(
                    expanded         = true,
                    onDismissRequest = onDismiss,
                    modifier         = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    if (suggestions.isEmpty()) {
                        DropdownMenuItem(
                            text    = {
                                Text(
                                    "No results found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = onDismiss
                        )
                    } else {
                        suggestions.forEach { s ->
                            DropdownMenuItem(
                                text    = { Text(s.name) },
                                onClick = { onSuggestionSelected(s) }
                            )
                        }
                    }
                }
            }
        }

        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error)
        }
        if (searchError != null) {
            Text(searchError, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MedTextField(
    value         : String,
    onValueChange : (String) -> Unit,
    label         : String,
    placeholder   : String,
    singleLine    : Boolean = true,
    error         : String? = null,
    keyboardType  : androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = singleLine,
            isError       = error != null,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType)
        )
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun MedSelector(
    label  : String,
    value  : String,
    icon   : androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier              = Modifier.padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    value,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    icon, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Keep old names so any stale references compile
@Composable
fun RacingTextField(
    value: String, onValueChange: (String) -> Unit,
    label: String, placeholder: String,
    singleLine: Boolean = true, error: String? = null
) = MedTextField(value, onValueChange, label, placeholder, singleLine, error)

@Composable
fun RacingSelector(
    label: String, value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit
) = MedSelector(label, value, icon, onClick)
