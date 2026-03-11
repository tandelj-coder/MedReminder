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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onEditMedication: (Int) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    val isGoalReached = uiState.adherenceStats.totalScheduled > 0 && 
                        uiState.adherenceStats.totalTaken == uiState.adherenceStats.totalScheduled

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "PADDOCK HUB",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                            color = RacingRed,
                            fontStyle = FontStyle.Italic
                        )
                        Text(
                            SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { viewModel.onResetTodayRequested() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TelemetryCard(
                            modifier = Modifier.weight(1.2f),
                            label = "SESSION PROGRESS",
                            value = "${uiState.adherenceStats.totalTaken}/${uiState.adherenceStats.totalScheduled}",
                            unit = "LAPS",
                            color = if (isGoalReached) RacingTeal else RacingRed,
                            icon = if (isGoalReached) "🏁" else "🏎️"
                        )
                        
                        TelemetryCard(
                            modifier = Modifier.weight(0.8f),
                            label = "STREAK",
                            value = "${uiState.adherenceStats.currentStreak}",
                            unit = "DAYS",
                            color = RacingOrange,
                            icon = "🔥"
                        )
                    }
                }

                item {
                    val progress = if (uiState.adherenceStats.totalScheduled > 0) 
                        uiState.adherenceStats.totalTaken.toFloat() / uiState.adherenceStats.totalScheduled 
                    else 0f
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TRACK COMPLETION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = RacingTeal, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = RacingTeal,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }

                uiState.nextDoseInfo?.let {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = RacingRed.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RacingRed.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("NEXT PIT STOP", style = MaterialTheme.typography.labelSmall, color = RacingRed, fontWeight = FontWeight.Black)
                                    Text(it.medicationName.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        formatMillis(it.remainingTimeMillis),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                    Text("UNTIL GREEN FLAG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                val pending = uiState.medications.filter { !it.isTakenToday }
                val taken = uiState.medications.filter { it.isTakenToday }

                if (pending.isNotEmpty()) {
                    item { SectionHeader(title = "UPCOMING SESSIONS", color = RacingRed) }
                    items(pending, key = { it.id }) { med ->
                        SwipeablePaddockMedicationItem(
                            medication = med,
                            onSwipeConfirm = { viewModel.onMarkAsTaken(med) },
                            onDelete = { viewModel.onDeleteMedication(med) },
                            onEdit = { onEditMedication(med.id) }
                        )
                    }
                }

                if (taken.isNotEmpty()) {
                    item { SectionHeader(title = "COMPLETED LAPS", color = RacingTeal) }
                    items(taken, key = { it.id }) { med ->
                        SwipeablePaddockMedicationItem(
                            medication = med,
                            onSwipeConfirm = {}, // Already taken
                            onDelete = { viewModel.onDeleteMedication(med) },
                            onEdit = { onEditMedication(med.id) }
                        )
                    }
                }
            }

            if (isGoalReached) { CheckeredFlagCelebration() }
        }
    }

    // Double Dose Dialog (F1 Racing Theme)
    uiState.doubleDoseWarning?.let { warning ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDoubleDoseWarning() },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = RacingRed,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            icon = { Icon(Icons.Default.Warning, null, tint = RacingRed, modifier = Modifier.size(40.dp)) },
            title = { Text("DOUBLE DOSE WARNING", fontWeight = FontWeight.Black) },
            text = {
                Text(
                    "You already completed this lap ${warning.minutesSinceLastDose} minutes ago. Entering the pits again may be dangerous. Confirm additional pit stop?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmDoubleDose(warning.medication) },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed)
                ) { Text("CONFIRM", fontWeight = FontWeight.Black, color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDoubleDoseWarning() }) { 
                    Text("ABORT", color = MaterialTheme.colorScheme.onSurface) 
                }
            }
        )
    }
}

@Composable
fun TelemetryCard(modifier: Modifier = Modifier, label: String, value: String, unit: String, color: Color, icon: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Text(icon, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Box(modifier = Modifier.size(12.dp, 2.dp).background(color))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
fun SwipeablePaddockMedicationItem(
    medication: Medication,
    onSwipeConfirm: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX)
    val isTaken = medication.isTakenToday

    Box(modifier = Modifier.fillMaxWidth()) {
        // Swipe Backgrounds
        if (offsetX > 0 && !isTaken) {
            Box(Modifier.fillMaxSize().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(RacingTeal.copy(alpha = 0.2f)).padding(start = 24.dp), contentAlignment = Alignment.CenterStart) {
                Icon(Icons.Default.Check, null, tint = RacingTeal)
            }
        } else if (offsetX < 0) {
            Box(Modifier.fillMaxSize().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(RacingRed.copy(alpha = 0.2f)).padding(end = 24.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Default.Delete, null, tint = RacingRed)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(isTaken) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX > 200f && !isTaken) onSwipeConfirm()
                            else if (offsetX < -200f) onDelete()
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-250f, 250f)
                        }
                    )
                }
                .clickable { onEdit() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            ListItem(
                headlineContent = { Text(medication.name.uppercase(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface) },
                supportingContent = { Text("${medication.dosage} MG • ${medication.formattedTime()}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold) },
                leadingContent = {
                    Box(
                        modifier = Modifier.size(52.dp).clip(CircleShape)
                            .background(if (isTaken) RacingTeal.copy(alpha = 0.1f) else RacingRed.copy(alpha = 0.1f))
                            .border(2.dp, if (isTaken) RacingTeal.copy(alpha = 0.3f) else RacingRed.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(medication.name.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = if (isTaken) RacingTeal else RacingRed)
                    }
                },
                trailingContent = {
                    if (isTaken) Icon(Icons.Default.Check, null, tint = RacingTeal, modifier = Modifier.size(28.dp))
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}

@Composable
fun CheckeredFlagCelebration() {
    val onBg = MaterialTheme.colorScheme.onBackground
    Box(modifier = Modifier.fillMaxSize().drawBehind {
        val size = 40.dp.toPx()
        for (x in 0..20) for (y in 0..40) if ((x + y) % 2 == 0) drawRect(color = onBg.copy(alpha = 0.03f), topLeft = Offset(x * size, y * size), size = androidx.compose.ui.geometry.Size(size, size))
    }, contentAlignment = Alignment.BottomCenter) {
        Surface(modifier = Modifier.padding(bottom = 32.dp), color = RacingTeal, shape = RoundedCornerShape(8.dp)) {
            Text("P1 - SESSION COMPLETE 🏁", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 2.sp)
        }
    }
}

private fun formatMillis(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
