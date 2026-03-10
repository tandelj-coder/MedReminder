package ca.sheridancollege.medreminder.presentation.today

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.Medication
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun TodayScreen(
    onEditMedication: (Int) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            // Minimalist Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date()).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "Overview",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (uiState.medications.any { it.isTakenToday }) {
                    IconButton(onClick = { viewModel.onResetTodayRequested() }) {
                        Icon(
                            Icons.Outlined.RestartAlt,
                            contentDescription = "Reset",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Simple Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "Done", value = "${uiState.adherenceStats.totalTaken}/${uiState.adherenceStats.totalScheduled}")
                VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                StatItem(label = "Streak", value = "${uiState.adherenceStats.currentStreak}d")
            }

            // Next Dose Timer (Minimal)
            uiState.nextDoseInfo?.let { info ->
                Spacer(Modifier.height(16.dp))
                NextDoseCountdown(info = info)
            }

            Spacer(Modifier.height(32.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                }
            } else if (uiState.medications.isEmpty()) {
                EmptyState()
            } else {
                val pending = uiState.medications.filter { !it.isTakenToday }
                val taken = uiState.medications.filter { it.isTakenToday }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    if (pending.isNotEmpty()) {
                        item { SectionLabel("UPCOMING") }
                        items(pending, key = { it.id }) { med ->
                            MinimalMedicationCard(
                                medication = med,
                                onSwipeConfirm = { viewModel.onMarkAsTaken(med) },
                                onDelete = { viewModel.onDeleteMedication(med) },
                                onEdit = { onEditMedication(med.id) }
                            )
                        }
                    }
                    if (taken.isNotEmpty()) {
                        item { Spacer(Modifier.height(8.dp)); SectionLabel("COMPLETED") }
                        items(taken, key = { it.id }) { med ->
                            MinimalMedicationCard(
                                medication = med,
                                onSwipeConfirm = { viewModel.onMarkAsTaken(med) },
                                onDelete = { viewModel.onDeleteMedication(med) },
                                onEdit = { onEditMedication(med.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs remain simple
    if (uiState.showResetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onResetTodayDismissed() },
            title = { Text("Reset Progress", fontWeight = FontWeight.Light) },
            text = { Text("Clear today's logs?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onResetTodayConfirmed() }) { Text("RESET") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onResetTodayDismissed() }) { Text("CANCEL") }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun NextDoseCountdown(info: NextDoseInfo) {
    val totalSeconds = (info.remainingTimeMillis / 1000).coerceAtLeast(0)
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("NEXT DOSE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(info.medicationName, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MinimalMedicationCard(
    medication: Medication,
    onSwipeConfirm: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(
                1.dp,
                if (medication.isTakenToday) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (medication.isTakenToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
            .clickable { onEdit() }
            .pointerInput(medication.isTakenToday) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 200f && !medication.isTakenToday) onSwipeConfirm()
                        else if (offsetX < -200f) onDelete()
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-250f, 250f)
                    }
                )
            }
    ) {
        // Swipe Backgrounds (Minimalist)
        if (offsetX > 0) {
            Box(Modifier.fillMaxSize().background(Color(0xFF2A9D8F).copy(alpha = 0.1f)).padding(start = 24.dp), contentAlignment = Alignment.CenterStart) {
                Icon(Icons.Default.Check, null, tint = Color(0xFF2A9D8F))
            }
        } else if (offsetX < 0) {
            Box(Modifier.fillMaxSize().background(Color(0xFFE63946).copy(alpha = 0.1f)).padding(end = 24.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFE63946))
            }
        }

        // Foreground
        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .background(if (medication.isTakenToday) Color.Transparent else MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                medication.formattedTime().split(" ")[0],
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Light,
                color = if (medication.isTakenToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    medication.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (medication.isTakenToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    medication.dosage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (medication.isTakenToday) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2A9D8F), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(Modifier.fillMaxSize().padding(bottom = 100.dp), contentAlignment = Alignment.Center) {
        Text(
            "Nothing scheduled.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Light
        )
    }
}
