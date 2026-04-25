package ca.sheridancollege.medreminder.presentation.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.DrugSuggestion
import ca.sheridancollege.medreminder.domain.model.MedicationType
import ca.sheridancollege.medreminder.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationScreen(
    medicationId: Int = 0,
    onNavigateBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    LaunchedEffect(medicationId) {
        if (medicationId != 0) {
            viewModel.loadMedication(medicationId)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .blur(100.dp)
                .clip(CircleShape)
                .background(F1Red.copy(alpha = 0.15f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (medicationId == 0) "NEW ENTRY" else "EDIT ENTRY",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Medication name with drug autocomplete
                DrugAutocompleteField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    suggestions = uiState.drugSuggestions,
                    isSearching = uiState.isSearching,
                    showSuggestions = uiState.showSuggestions,
                    searchError = uiState.searchError,
                    onSuggestionSelected = viewModel::onDrugSelected,
                    onDismiss = viewModel::onDismissSuggestions,
                    error = uiState.nameError
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RacingTextField(
                        value = uiState.dosageAmount,
                        onValueChange = viewModel::onDosageAmountChange,
                        label = "AMOUNT",
                        placeholder = "500",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    RacingTextField(
                        value = uiState.dosageUnit,
                        onValueChange = viewModel::onDosageUnitChange,
                        label = "UNIT",
                        placeholder = "mg",
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Column {
                    Text("MEDICATION TYPE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Text(
                                uiState.medicationType.displayName,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            MedicationType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        viewModel.onMedicationTypeChange(type)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("REMINDER TIMES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    uiState.times.forEachIndexed { index, time ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RacingSelector(
                                label = "",
                                value = time.formatted(),
                                icon = Icons.Outlined.Schedule,
                                onClick = {
                                    TimePickerDialog(context, { _, h, m ->
                                        viewModel.onTimeChange(index, h, m)
                                    }, time.hour, time.minute, false).show()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.times.size > 1) {
                                IconButton(onClick = { viewModel.onRemoveTime(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Time", tint = F1Red)
                                }
                            }
                        }
                    }
                    TextButton(onClick = viewModel::onAddTime, colors = ButtonDefaults.textButtonColors(contentColor = F1Red)) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("ADD ANOTHER TIME")
                    }
                }

                Column {
                    Text("SCHEDULE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            val isSelected = uiState.selectedDays.contains(day)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onDayToggled(day) },
                                label = { Text(day.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = F1Red,
                                    selectedLabelColor = Color.White,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                    if (uiState.daysError != null) {
                        Text(text = uiState.daysError!!, style = MaterialTheme.typography.bodySmall, color = F1Red, modifier = Modifier.padding(top = 8.dp))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RacingSelector(
                        label = "START DATE",
                        value = dateFormatter.format(Date(uiState.startDate)),
                        icon = Icons.Default.CalendarMonth,
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = uiState.startDate }
                            DatePickerDialog(context, { _, y, m, d ->
                                val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                viewModel.onStartDateChange(newCal.timeInMillis)
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    RacingSelector(
                        label = "END DATE (OPTIONAL)",
                        value = uiState.endDate?.let { dateFormatter.format(Date(it)) } ?: "INDEFINITE",
                        icon = Icons.Default.CalendarMonth,
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = uiState.endDate ?: System.currentTimeMillis() }
                            DatePickerDialog(context, { _, y, m, d ->
                                val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                viewModel.onEndDateChange(newCal.timeInMillis)
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
                                setButton(DatePickerDialog.BUTTON_NEUTRAL, "CLEAR") { _, _ -> viewModel.onEndDateChange(null) }
                            }.show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                RacingTextField(
                    value = uiState.instructions,
                    onValueChange = viewModel::onInstructionsChange,
                    label = "INSTRUCTIONS (e.g. After food)",
                    placeholder = "TAKE WITH WATER..."
                )

                RacingTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = "PRIVATE NOTES",
                    placeholder = "ADD NOTES...",
                    singleLine = false
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = viewModel::onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(F1Red, Color(0xFF8B0000)))),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    elevation = null,
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                    } else {
                        Text("CONFIRM SESSION", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun DrugAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<DrugSuggestion>,
    isSearching: Boolean,
    showSuggestions: Boolean,
    searchError: String?,
    onSuggestionSelected: (DrugSuggestion) -> Unit,
    onDismiss: () -> Unit,
    error: String? = null
) {
    Column {
        Text("MEDICATION NAME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Box {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("ENTER NAME...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (error != null) F1Red else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = F1Red,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = error != null,
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = F1Red, strokeWidth = 2.dp)
                    }
                }
            )

            if (showSuggestions) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    if (suggestions.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "No results found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = onDismiss
                        )
                    } else {
                        suggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        suggestion.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { onSuggestionSelected(suggestion) }
                            )
                        }
                    }
                }
            }
        }

        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = F1Red, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
        }
        if (searchError != null) {
            Text(searchError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
        }
    }
}

@Composable
fun RacingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (error != null) F1Red else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = F1Red,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            isError = error != null,
            keyboardOptions = keyboardOptions
        )
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = F1Red,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun RacingSelector(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.clickable { onClick() }) {
        if (label.isNotBlank()) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                Icon(icon, null, tint = F1Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}
