package ca.sheridancollege.medreminder.presentation.add

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.ui.theme.*
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
            .background(DeepPaddock)
    ) {
        // Racing Ambient Glow
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
                            color = Color.White,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Black
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
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

                // Racing Style Inputs
                RacingTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = "MEDICATION NAME",
                    placeholder = "ENTER NAME...",
                    error = uiState.nameError
                )

                RacingTextField(
                    value = uiState.dosage,
                    onValueChange = viewModel::onDosageChange,
                    label = "DOSAGE (mg)",
                    placeholder = "ENTER DOSAGE (e.g. 500)..."
                )

                // Time Selector (Racing Style)
                RacingSelector(
                    label = "REMINDER TIME",
                    value = String.format(Locale.getDefault(), "%02d:%02d", uiState.timeHour, uiState.timeMinute),
                    icon = Icons.Outlined.Schedule,
                    onClick = {
                        TimePickerDialog(context, { _, h, m ->
                            viewModel.onTimeChange(h, m)
                        }, uiState.timeHour, uiState.timeMinute, false).show()
                    }
                )

                // Day Selector (Racing Chips)
                Column {
                    Text("FREQUENCY", style = MaterialTheme.typography.labelSmall, color = TextGray, fontWeight = FontWeight.Bold)
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
                                    labelColor = TextGray,
                                    containerColor = CardDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) Color.Transparent else GlassBorder,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                    if (uiState.daysError != null) {
                        Text(
                            text = uiState.daysError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = F1Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                RacingTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = "NOTES (OPTIONAL)",
                    placeholder = "ADD NOTES...",
                    singleLine = false
                )

                Spacer(Modifier.height(32.dp))

                // High-Contrast Racing Button
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
fun RacingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    error: String? = null
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextGray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextGray.copy(alpha = 0.5f)) },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (error != null) F1Red else GlassBorder, RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark,
                disabledContainerColor = CardDark,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = F1Red,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            isError = error != null
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
fun RacingSelector(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextGray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = CardDark,
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(value, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Black)
                Icon(icon, null, tint = F1Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}
