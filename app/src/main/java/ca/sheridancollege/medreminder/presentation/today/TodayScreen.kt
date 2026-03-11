package ca.sheridancollege.medreminder.presentation.today

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontStyle
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepPaddock)
            .drawBehind {
                val path = Path().apply {
                    moveTo(size.width * 0.7f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width * 0.8f, size.height)
                    lineTo(size.width * 0.5f, size.height)
                    close()
                }
                drawPath(path, color = Color.White.copy(alpha = 0.02f))
            }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
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
                                color = TextGray,
                                letterSpacing = 1.sp
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
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        scrolledContainerColor = CardDark.copy(alpha = 0.9f)
                    ),
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
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
                            Text("TRACK COMPLETION", style = MaterialTheme.typography.labelSmall, color = TextGray, fontWeight = FontWeight.Bold)
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = RacingTeal, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = RacingTeal,
                            trackColor = CardDark,
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
                                    Text(it.medicationName.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        formatMillis(it.remainingTimeMillis),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                    Text("UNTIL GREEN FLAG", style = MaterialTheme.typography.labelSmall, color = TextGray)
                                }
                            }
                        }
                    }
                }

                val pending = uiState.medications.filter { !it.isTakenToday }
                val taken = uiState.medications.filter { it.isTakenToday }

                if (pending.isNotEmpty()) {
                    item { 
                        SectionHeader(title = "UPCOMING SESSIONS", color = RacingRed)
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
                        SectionHeader(title = "COMPLETED LAPS", color = RacingTeal)
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

            if (isGoalReached) {
                CheckeredFlagCelebration()
            }
        }
    }
}

@Composable
fun TelemetryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    color: Color,
    icon: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = CardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextGray, fontWeight = FontWeight.Bold)
                Text(icon, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.labelSmall, color = TextGray, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Box(modifier = Modifier.size(12.dp, 2.dp).background(color))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
fun PaddockMedicationItem(
    medication: Medication,
    onMark: () -> Unit,
    onEdit: () -> Unit
) {
    val isTaken = medication.isTakenToday
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isTaken) CardDark.copy(alpha = 0.5f) else CardDark),
        border = if (!isTaken) androidx.compose.foundation.BorderStroke(1.dp, GlassBorder) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isTaken) RacingTeal.copy(alpha = 0.1f) else RacingRed.copy(alpha = 0.1f))
                    .border(2.dp, if (isTaken) RacingTeal.copy(alpha = 0.3f) else RacingRed.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    medication.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isTaken) RacingTeal else RacingRed
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    medication.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isTaken) TextGray else Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "${medication.dosage} MG • ${medication.formattedTime()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!isTaken) {
                FilledIconButton(
                    onClick = onMark,
                    shape = RoundedCornerShape(8.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = RacingTeal)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Mark Taken", tint = DeepPaddock)
                }
            } else {
                Icon(Icons.Default.Check, contentDescription = "Taken", tint = RacingTeal, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun CheckeredFlagCelebration() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.1f,
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
            .drawBehind {
                val size = 40.dp.toPx()
                for (x in 0..20) {
                    for (y in 0..40) {
                        if ((x + y) % 2 == 0) {
                            drawRect(
                                color = Color.White.copy(alpha = 0.03f),
                                topLeft = Offset(x * size, y * size),
                                size = androidx.compose.ui.geometry.Size(size, size)
                            )
                        }
                    }
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.padding(bottom = 32.dp),
            color = RacingTeal,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "P1 - SESSION COMPLETE 🏁",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Black,
                color = DeepPaddock,
                letterSpacing = 2.sp
            )
        }
    }
}

private fun formatMillis(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
