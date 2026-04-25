package ca.sheridancollege.medreminder.presentation.today

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.DoseEvent
import ca.sheridancollege.medreminder.ui.theme.MedMissed
import ca.sheridancollege.medreminder.ui.theme.MedTaken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(viewModel: TodayViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    val totalScheduled = uiState.adherenceStats.totalScheduled
    val totalTaken     = uiState.adherenceStats.totalTaken
    val allDone        = totalScheduled > 0 && totalTaken == totalScheduled

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "MedReminder",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onResetTodayRequested() }) {
                        Icon(
                            Icons.Default.Refresh, "Reset today",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier        = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding  = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Progress card ──────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier            = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Daily Progress",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "$totalTaken of $totalScheduled doses taken",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            if (allDone) {
                                Surface(
                                    shape = CircleShape,
                                    color = MedTaken.copy(alpha = 0.15f)
                                ) {
                                    Icon(
                                        Icons.Default.Check, null,
                                        tint     = MedTaken,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(24.dp)
                                    )
                                }
                            }
                        }
                        val progress = if (totalScheduled > 0)
                            totalTaken.toFloat() / totalScheduled else 0f
                        LinearProgressIndicator(
                            progress  = { progress },
                            modifier  = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color      = if (allDone) MedTaken else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // ── Low stock warning ──────────────────────────────────────
            if (uiState.lowStockMedications.isNotEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier          = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning, null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Low stock: ${uiState.lowStockMedications.joinToString { it.name }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // ── Next dose countdown ────────────────────────────────────
            uiState.nextDoseInfo?.let { next ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier              = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Next dose",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    next.medicationName,
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Text(
                                formatMillis(next.remainingTimeMillis),
                                style      = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // ── Upcoming (scheduled) doses ─────────────────────────────
            val pending = uiState.doseEvents.filter { it.isScheduled() }
            val taken   = uiState.doseEvents.filter { it.isTaken() }
            val missed  = uiState.doseEvents.filter { it.isMissed() }

            if (pending.isNotEmpty()) {
                item {
                    Text(
                        "Upcoming",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground,
                        modifier   = Modifier.padding(top = 4.dp)
                    )
                }
                items(pending, key = { it.id }) { dose ->
                    SwipeableDoseCard(
                        doseEvent   = dose,
                        onMarkTaken = { viewModel.onMarkAsTaken(dose) },
                        onDelete    = { viewModel.onDeleteMedication(dose) }
                    )
                }
            }

            if (missed.isNotEmpty()) {
                item {
                    Text(
                        "Missed",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MedMissed,
                        modifier   = Modifier.padding(top = 4.dp)
                    )
                }
                items(missed, key = { it.id }) { dose ->
                    SwipeableDoseCard(
                        doseEvent   = dose,
                        onMarkTaken = { viewModel.onMarkAsTaken(dose) },
                        onDelete    = { viewModel.onDeleteMedication(dose) }
                    )
                }
            }

            if (taken.isNotEmpty()) {
                item {
                    Text(
                        "Completed",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MedTaken,
                        modifier   = Modifier.padding(top = 4.dp)
                    )
                }
                items(taken, key = { it.id }) { dose ->
                    SwipeableDoseCard(
                        doseEvent   = dose,
                        onMarkTaken = {},
                        onDelete    = { viewModel.onDeleteMedication(dose) }
                    )
                }
            }

            if (uiState.doseEvents.isEmpty() && !uiState.isLoading) {
                item { EmptyDoseState() }
            }
        }
    }

    // ── Double-dose warning dialog ─────────────────────────────────────
    uiState.doubleDoseWarning?.let { warning ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDoubleDoseWarning() },
            icon  = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)) },
            title = { Text("Double Dose Warning", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "${warning.doseEvent.medicationName} was already recorded " +
                    "${warning.minutesSinceLastDose} min ago. " +
                    "Are you sure you want to record another dose?"
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmDoubleDose(warning.doseEvent) },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Confirm") }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.onDismissDoubleDoseWarning() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Reset confirmation dialog ──────────────────────────────────────
    if (uiState.showResetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onResetTodayDismissed() },
            title = { Text("Reset Today?") },
            text  = { Text("This will clear all dose records for today.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onResetTodayConfirmed() },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Reset") }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.onResetTodayDismissed() }) { Text("Cancel") }
            }
        )
    }

    // ── Snackbar ───────────────────────────────────────────────────────
    uiState.snackbarMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2500)
            viewModel.onSnackbarDismissed()
        }
    }
}

// ── Swipeable dose card ────────────────────────────────────────────────────

@Composable
fun SwipeableDoseCard(
    doseEvent   : DoseEvent,
    onMarkTaken : () -> Unit,
    onDelete    : () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "offset")
    val isTaken  = doseEvent.isTaken()
    val isMissed = doseEvent.isMissed()

    val accentColor = when {
        isTaken  -> MedTaken
        isMissed -> MedMissed
        else     -> MaterialTheme.colorScheme.primary
    }

    val scheduledTime = SimpleDateFormat("h:mm a", Locale.getDefault())
        .format(Date(doseEvent.scheduledTime))

    Box(modifier = Modifier.fillMaxWidth()) {
        // Swipe-right reveal (mark taken)
        if (offsetX > 0 && !isTaken) {
            Box(
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MedTaken.copy(alpha = 0.15f))
                    .padding(start = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) { Icon(Icons.Default.Check, null, tint = MedTaken) }
        }
        // Swipe-left reveal (delete)
        if (offsetX < 0) {
            Box(
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }

        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(isTaken) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX > 150f && !isTaken) onMarkTaken()
                            else if (offsetX < -150f) onDelete()
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, drag ->
                            offsetX = (offsetX + drag).coerceIn(-220f, 220f)
                        }
                    )
                },
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier              = Modifier.padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f))
                        .border(1.5.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTaken) {
                        Icon(Icons.Default.Check, null,
                            tint = accentColor, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            doseEvent.medicationName.take(1).uppercase(),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = accentColor
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        doseEvent.medicationName,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        scheduledTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        when {
                            isTaken  -> "Taken"
                            isMissed -> "Missed"
                            else     -> "Scheduled"
                        },
                        style      = MaterialTheme.typography.labelSmall,
                        color      = accentColor,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDoseState() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Medication, null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "No doses scheduled for today",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Add a medication to get started",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatMillis(millis: Long): String {
    val total   = (millis / 1000).coerceAtLeast(0)
    val hours   = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    return if (hours > 0)
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    else
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
