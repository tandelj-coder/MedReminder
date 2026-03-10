package ca.sheridancollege.medreminder.presentation.today

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color(0xFF0D2447),
                                androidx.compose.ui.graphics.Color(0xFF1A3A6E),
                                MaterialTheme.colorScheme.background
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Today's Medications",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (uiState.medications.any { it.isTakenToday }) {
                            IconButton(onClick = { viewModel.onResetTodayRequested() }) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Reset today",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    AdherenceCard(
                        taken = uiState.adherenceStats.totalTaken,
                        total = uiState.adherenceStats.totalScheduled,
                        streak = uiState.adherenceStats.currentStreak,
                        weeklyAdherence = uiState.adherenceStats.weeklyAdherence
                    )

                    // Next dose countdown
                    uiState.nextDoseInfo?.let { info ->
                        Spacer(Modifier.height(10.dp))
                        NextDoseCountdown(info = info)
                    }
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.medications.isEmpty()) {
                EmptyState()
            } else {
                val taken = uiState.medications.filter { it.isTakenToday }
                val pending = uiState.medications.filter { !it.isTakenToday }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (pending.isNotEmpty()) {
                        item {
                            SectionHeader("Pending", pending.size, MaterialTheme.colorScheme.error)
                        }
                        items(pending, key = { it.id }) { med ->
                            SwipeableMedicationCard(
                                medication = med,
                                onSwipeConfirm = { viewModel.onMarkAsTaken(med) },
                                onDelete = { viewModel.onDeleteMedication(med) },
                                onEdit = { onEditMedication(med.id) }
                            )
                        }
                    }
                    if (taken.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            SectionHeader("Taken", taken.size, Color(0xFF4CAF50))
                        }
                        items(taken, key = { it.id }) { med ->
                            SwipeableMedicationCard(
                                medication = med,
                                onSwipeConfirm = { viewModel.onMarkAsTaken(med) },
                                onDelete = { viewModel.onDeleteMedication(med) },
                                onEdit = { onEditMedication(med.id) }
                            )
                        }
                    }
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "← Swipe left to delete  •  Swipe right to confirm  •  Tap to edit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Double dose dialog
    uiState.doubleDoseWarning?.let { warning ->
        DoubleDoseWarningDialog(
            medicationName = warning.medication.name,
            minutesSince = warning.minutesSinceLastDose,
            lastTakenAt = warning.lastTakenAt,
            onDismiss = { viewModel.onDismissDoubleDoseWarning() },
            onConfirm = { viewModel.onConfirmDoubleDose(warning.medication) }
        )
    }

    // Reset confirm dialog
    if (uiState.showResetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onResetTodayDismissed() },
            icon = { Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Reset Today?") },
            text = { Text("This will mark all medications as not taken for today. Your history log will be kept.") },
            confirmButton = {
                Button(onClick = { viewModel.onResetTodayConfirmed() }) { Text("Reset") }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.onResetTodayDismissed() }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun NextDoseCountdown(info: NextDoseInfo) {
    val totalSeconds = (info.remainingTimeMillis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val countdownText = when {
        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
        else -> "${minutes}m ${seconds}s"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Alarm,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        "Next dose",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        info.medicationName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                countdownText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Badge(containerColor = color) {
            Text("$count", fontSize = 10.sp, color = Color.White)
        }
    }
}

@Composable
fun SwipeableMedicationCard(
    medication: Medication,
    onSwipeConfirm: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val swipeThreshold = 220f
    val deleteThreshold = -220f
    var offsetX by remember { mutableFloatStateOf(0f) }

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe"
    )

    val swipeProgress = (animatedOffset / swipeThreshold).coerceIn(0f, 1f)
    val deleteProgress = (-animatedOffset / -deleteThreshold).coerceIn(0f, 1f)

    val cardColor = when {
        medication.isTakenToday -> MaterialTheme.colorScheme.secondaryContainer
        animatedOffset > 0 -> lerp(MaterialTheme.colorScheme.surface, Color(0xFF4CAF50), swipeProgress)
        animatedOffset < 0 -> lerp(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.errorContainer, deleteProgress)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (!medication.isTakenToday && animatedOffset >= 0) {
            Box(
                modifier = Modifier.fillMaxWidth().height(88.dp)
                    .clip(RoundedCornerShape(20.dp)).background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier.padding(start = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Text("Mark Taken", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (animatedOffset <= 0) {
            Box(
                modifier = Modifier.fillMaxWidth().height(88.dp)
                    .clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(modifier = Modifier.padding(end = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .clickable { onEdit() }
                .pointerInput(medication.isTakenToday) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > swipeThreshold && !medication.isTakenToday -> onSwipeConfirm()
                                offsetX < deleteThreshold -> onDelete()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = { offsetX = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(deleteThreshold - 20f, swipeThreshold + 20f)
                        }
                    )
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                        .background(
                            if (medication.isTakenToday) Color(0xFF4CAF50).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (medication.isTakenToday) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(28.dp))
                    } else {
                        Text(
                            medication.formattedTime().split(" ")[0],
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(medication.name, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    if (medication.dosage.isNotBlank()) {
                        Text(medication.dosage, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(12.dp),
                            tint = if (medication.isTakenToday) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary)
                        Text(
                            if (medication.isTakenToday && medication.takenTimestamp != null)
                                "Taken at ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(medication.takenTimestamp))}"
                            else medication.formattedTime(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (medication.isTakenToday) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        medication.days.take(3).forEach { day ->
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(20.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(day.code.take(1), style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        if (medication.days.size > 3) {
                            Text("+${medication.days.size - 3}", style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Default.Edit, contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun AdherenceCard(taken: Int, total: Int, streak: Int,
    weeklyAdherence: List<ca.sheridancollege.medreminder.domain.model.DayAdherence> = emptyList()) {
    val progress = if (total > 0) taken.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "progress"
    )
    val flameScale = 1f
    val milestones = listOf(7, 14, 30, 60, 100)
    val nextMilestone = milestones.firstOrNull { it > streak }
    val motivational = when {
        streak == 0 -> "Start your streak today!"
        streak < 3 -> "Great start! Keep going 💪"
        streak < 7 -> "Almost a week! Don't break it!"
        streak < 14 -> "One week strong! 🔥"
        streak < 30 -> "Two weeks! You're on fire!"
        streak < 60 -> "One month streak! Incredible!"
        else -> "Legend status! 🏆"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Top row: doses + streak
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("$taken / $total doses", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text(if (taken == total && total > 0) "All done! 🎉" else "taken today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Flame + streak count
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥", fontSize = 28.sp)
                    Text("$streak", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("day streak", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Motivational message
            Surface(shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                Text(motivational,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium)
            }

            // 7-day dots
            if (weeklyAdherence.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    weeklyAdherence.forEach { day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(if (day.isToday) 32.dp else 28.dp)
                                    .clip(CircleShape)
                                    .background(when {
                                        day.isToday && day.tookAll -> Color(0xFF4CAF50)
                                        day.isToday -> MaterialTheme.colorScheme.primaryContainer
                                        day.tookAll -> Color(0xFF4CAF50).copy(alpha = 0.7f)
                                        day.hadMedications -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day.tookAll) {
                                    Icon(Icons.Default.Check, null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp))
                                } else if (day.isToday) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary))
                                }
                            }
                            Text(day.label, style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (day.isToday) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (taken == total && total > 0) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Next milestone
            if (nextMilestone != null && streak > 0) {
                val remaining = nextMilestone - streak
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.EmojiEvents, null,
                        tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Text("$remaining more days to $nextMilestone-day milestone!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}


@Composable
fun DoubleDoseWarningDialog(
    medicationName: String, minutesSince: Long, lastTakenAt: Long,
    onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    val lastTakenTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(lastTakenAt))
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) },
        title = { Text("Double Dose Warning", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("You already took $medicationName at $lastTakenTime.")
                Text("That was only $minutesSince minute(s) ago.", fontWeight = FontWeight.SemiBold)
                Text("Taking it again this soon may be dangerous. Are you absolutely sure?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Yes, I'm sure") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("💊", style = MaterialTheme.typography.displayLarge)
            Text("No medications today", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Tap + to add your first medication", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}
