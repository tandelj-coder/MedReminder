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

@OptIn(ExperimentalMaterial3Api::class)
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
            .background(Brush.verticalGradient(SurfaceGradient))
    ) {
        // Aesthetic Blurred Background Elements
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 50.dp)
                .blur(80.dp)
                .clip(CircleShape)
                .background(CyberTeal.copy(alpha = 0.15f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            if (medicationId == 0) "NEW REMINDER" else "EDIT REMINDER", 
                            style = MaterialTheme.typography.labelLarge, 
                            color = CyberTeal,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
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
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Aesthetic Input Fields using Glassmorphism
                AestheticTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = "MEDICATION NAME",
                    placeholder = "e.g. Aspirin",
                    error = uiState.nameError
                )

                AestheticTextField(
                    value = uiState.dosage,
                    onValueChange = viewModel::onDosageChange,
                    label = "DOSAGE",
                    placeholder = "e.g. 500mg"
                )

                // Time Selector
                AestheticSelector(
                    label = "REMINDER TIME",
                    value = String.format(Locale.getDefault(), "%02d:%02d", uiState.timeHour, uiState.timeMinute),
                    icon = Icons.Outlined.Schedule,
                    onClick = {
                        TimePickerDialog(context, { _, h, m ->
                            viewModel.onTimeChange(h, m)
                        }, uiState.timeHour, uiState.timeMinute, false).show()
                    }
                )

                // Day Selector
                Column {
                    Text("FREQUENCY", style = MaterialTheme.typography.labelSmall, color = SoftLavender, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            val isSelected = uiState.selectedDays.contains(day)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CyberTeal else GlassWhite)
                                    .border(BorderStroke(1.dp, if (isSelected) Color.Transparent else GlassBorder), CircleShape)
                                    .clickable { viewModel.onDayToggled(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    day.code.take(1),
                                    color = if (isSelected) MidnightBlue else Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (uiState.daysError != null) {
                        Text(
                            text = uiState.daysError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRose,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                AestheticTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = "NOTES (OPTIONAL)",
                    placeholder = "Take with food...",
                    singleLine = false
                )

                Spacer(Modifier.height(32.dp))

                // Gradient Button
                Button(
                    onClick = viewModel::onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(PrimaryGradient)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    elevation = null,
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("CONFIRM REMINDER", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun AestheticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    error: String? = null
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SoftLavender, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.3f)) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassWhite)
                .border(BorderStroke(1.dp, if (error != null) ErrorRose else GlassBorder), RoundedCornerShape(16.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = CyberTeal,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = singleLine,
            isError = error != null
        )
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = ErrorRose,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun AestheticSelector(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SoftLavender, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = GlassWhite,
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(value, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Icon(icon, null, tint = CyberTeal, modifier = Modifier.size(20.dp))
            }
        }
    }
}
