package ca.sheridancollege.medreminder.presentation.today

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
        containerColor = DeepPaddock,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "MEDREMINDER PADDOCK",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = RacingRed
                        )
                        Text(
                            SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.onResetTodayRequested() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = DeepPaddock,
                    titleContentColor = Color.White,
                    scrolledContainerColor = CardDark
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Racing Telemetry Summary
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isGoalReached) RacingTeal else CardDark
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    if (isGoalReached) "SESSION COMPLETE" else "DAILY TELEMETRY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGoalReached) DeepPaddock else RacingTeal
                                )
                                Text(
                                    "${uiState.adherenceStats.totalTaken}/${uiState.adherenceStats.totalScheduled} LAPS",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isGoalReached) Color.White else Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (isGoalReached) "🏁" else "🔥",
                                    style = MaterialTheme.typography.displaySmall
                                )
                                Text(
                                    "${uiState.adherenceStats.currentStreak}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (isGoalReached) Color.White else RacingRed
                                )
                            }
                        }
                    }
                }

                // Next Pit Stop (Countdown)
                uiState.nextDoseInfo?.let {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = CardDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, RacingTeal.copy(alpha = 0.3f))
                        ) {
                            ListItem(
                                headlineContent = { 
                                    Text("NEXT PIT STOP", style = MaterialTheme.typography.labelSmall, color = TextGray) 
                                },
                                supportingContent = { 
                                    Text(it.medicationName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) 
                                },
                                trailingContent = {
                                    Text(
                                        formatMillis(it.remainingTimeMillis),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = RacingTeal,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }

                val pending = uiState.medications.filter { !it.isTakenToday }
                val taken = uiState.medications.filter { it.isTakenToday }

                if (pending.isNotEmpty()) {
                    item { 
                        Text("AWAITING SESSION", style = MaterialTheme.typography.labelSmall, color = RacingRed, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    items(pending) { med ->
                        PaddockMedicationItem(
                            medication = med,
                            onMark = { viewModel.onMarkAsTaken(med) },
                            onEdit = { onEditMedication(med.id) }
                        )
                    }
                }

                if (taken.isNotEmpty()) {
                    item { 
                        Text("LAPS COMPLETED", style = MaterialTheme.typography.labelSmall, color = RacingTeal, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    items(taken) { med ->
                        PaddockMedicationItem(
                            medication = med,
                            onMark = {},
                            onEdit = { onEditMedication(med.id) }
                        )
                    }
                }
            }

            // Checkered Flag Celebration Overlay
            if (isGoalReached) {
                CheckeredFlagCelebration()
            }
        }
    }
}

@Composable
fun PaddockMedicationItem(
    medication: Medication,
    onMark: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        onClick = onEdit
    ) {
        ListItem(
            headlineContent = { Text(medication.name, fontWeight = FontWeight.Bold, color = Color.White) },
            supportingContent = { Text("${medication.dosage} • ${medication.formattedTime()}", color = TextGray) },
            leadingContent = {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (medication.isTakenToday) RacingTeal.copy(alpha = 0.2f) else RacingRed.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            medication.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (medication.isTakenToday) RacingTeal else RacingRed
                        )
                    }
                }
            },
            trailingContent = {
                if (!medication.isTakenToday) {
                    FilledIconButton(
                        onClick = onMark,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = RacingTeal)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Mark Taken", tint = DeepPaddock)
                    }
                } else {
                    Icon(Icons.Default.Check, contentDescription = "Taken", tint = RacingTeal)
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun CheckeredFlagCelebration() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = alpha), Color.Transparent)
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SESSION COMPLETE", fontWeight = FontWeight.Black, color = RacingTeal, letterSpacing = 4.sp)
            Text("🏁🏁🏁", fontSize = 24.sp)
        }
    }
}

private fun formatMillis(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format(Locale.getDefault(), "%02dm %02ds", minutes, seconds)
}
