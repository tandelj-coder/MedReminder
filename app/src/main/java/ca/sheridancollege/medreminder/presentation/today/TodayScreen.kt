package ca.sheridancollege.medreminder.presentation.today

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.Medication
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun TodayScreen(viewModel: TodayViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarDismissed()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                    .format(Date()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Today's Medications",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Adherence card
            AdherenceCard(uiState.adherenceStats.totalTaken,
                uiState.adherenceStats.totalScheduled,
                uiState.adherenceStats.currentStreak)

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.medications.isEmpty()) {
                EmptyState()
            } else {
                Text(
                    "Swipe right to confirm taken",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.medications, key = { it.id }) { med ->
                        SwipeableMedicationCard(
                            medication = med,
                            onSwipeConfirm = { viewModel.onMarkAsTaken(med) }
                        )
                    }
                }
            }
        }
    }

    // Double dose warning dialog
    uiState.doubleDoseWarning?.let { warning ->
        DoubleDoseWarningDialog(
            medicationName = warning.medication.name,
            minutesSince = warning.minutesSinceLastDose,
            lastTakenAt = warning.lastTakenAt,
            onDismiss = { viewModel.onDismissDoubleDoseWarning() },
            onConfirm = { viewModel.onConfirmDoubleDose(warning.medication) }
        )
    }
}

@Composable
fun SwipeableMedicationCard(
    medication: Medication,
    onSwipeConfirm: () -> Unit
) {
    val swipeThreshold = 200f
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe"
    )
    val progress = (animatedOffset / swipeThreshold).coerceIn(0f, 1f)
    val cardColor = if (medication.isTakenToday)
        MaterialTheme.colorScheme.secondaryContainer
    else lerp(MaterialTheme.colorScheme.surface,
        Color(0xFF4CAF50), progress)

    Box(modifier = Modifier.fillMaxWidth()) {
        // Green background hint
        if (!medication.isTakenToday) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.padding(start = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Taken!", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(medication.isTakenToday) {
                    if (!medication.isTakenToday) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > swipeThreshold) {
                                    onSwipeConfirm()
                                }
                                offsetX = 0f
                            },
                            onDragCancel = { offsetX = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                offsetX = (offsetX + dragAmount).coerceIn(0f, swipeThreshold + 20f)
                            }
                        )
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (medication.dosage.isNotBlank()) {
                        Text(
                            medication.dosage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        medication.formattedTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (medication.isTakenToday) {
                    Column(horizontalAlignment = Alignment.End) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(28.dp)
                        )
                        medication.takenTimestamp?.let { ts ->
                            Text(
                                SimpleDateFormat("h:mm a", Locale.getDefault())
                                    .format(Date(ts)),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                } else {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Swipe to confirm",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AdherenceCard(taken: Int, total: Int, streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$taken/$total",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Today",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        " $streak",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    "Day streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun DoubleDoseWarningDialog(
    medicationName: String,
    minutesSince: Long,
    lastTakenAt: Long,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val lastTakenTime = SimpleDateFormat("h:mm a", Locale.getDefault())
        .format(Date(lastTakenAt))

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Double Dose Warning",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("You already took $medicationName at $lastTakenTime.")
                Text(
                    "That was only $minutesSince minute(s) ago.",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Taking it again this soon may cause a double dose. Are you absolutely sure?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Yes, I'm sure") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💊", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(8.dp))
            Text("No medications today", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tap + to add your first medication",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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