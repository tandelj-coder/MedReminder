package ca.sheridancollege.medreminder.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.ui.theme.*
import java.util.*

@Composable
fun TodayScreen(
    onEditMedication: (Int) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DeepPaddock,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = F1Blue,
                    onClick = { /* Promo */ }
                ) {
                    Text("Get Pro", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Outlined.Person, null, tint = TextGray, modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Text("TODAY'S MEDICATIONS", style = MaterialTheme.typography.labelLarge, color = TextGray, letterSpacing = 1.sp)
            }

            // Streak & Progress Card (F1 Style)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = CardDark
                ) {
                    Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("${uiState.adherenceStats.totalTaken} / ${uiState.adherenceStats.totalScheduled} doses", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = F1Teal)
                            Text("taken today", color = TextGray)
                            Spacer(Modifier.height(16.dp))
                            Text("Start your streak today!", color = F1Teal, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔥", fontSize = 32.sp)
                            Text("${uiState.adherenceStats.currentStreak}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("day streak", color = TextGray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Countdown Timer (F1 FP1 Style)
            uiState.nextDoseInfo?.let {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = CardDark
                    ) {
                        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Next dose", color = TextGray, style = MaterialTheme.typography.labelLarge)
                                Text(it.medicationName, color = F1Teal, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                formatMillis(it.remainingTimeMillis),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = F1Teal
                            )
                        }
                    }
                }
            }

            // Stats Grid (2 columns)
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), color = CardDark) {
                        Column(Modifier.padding(16.dp)) {
                            Text("306.1 km", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Total Health", color = TextGray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), color = CardDark) {
                        Column(Modifier.padding(16.dp)) {
                            Text("58", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Laps Done", color = TextGray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            val pending = uiState.medications.filter { !it.isTakenToday }
            val taken = uiState.medications.filter { it.isTakenToday }

            if (pending.isNotEmpty()) {
                item { Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(F1Red))
                    Spacer(Modifier.width(8.dp))
                    Text("Pending", color = Color.White, fontWeight = FontWeight.Bold)
                    Badge(containerColor = F1Red, modifier = Modifier.padding(start = 8.dp)) { Text("${pending.size}", color = Color.White) }
                } }
                items(pending) { med ->
                    PaddockMedCard(med, onMark = { viewModel.onMarkAsTaken(med) }, onEdit = { onEditMedication(med.id) })
                }
            }

            if (taken.isNotEmpty()) {
                item { Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(F1Teal))
                    Spacer(Modifier.width(8.dp))
                    Text("Completed", color = Color.White, fontWeight = FontWeight.Bold)
                } }
                items(taken) { med ->
                    PaddockMedCard(med, onMark = {}, onEdit = { onEditMedication(med.id) })
                }
            }
        }
    }
}

@Composable
fun PaddockMedCard(med: Medication, onMark: () -> Unit, onEdit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        color = CardDark
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(med.formattedTime().split(" ")[0], color = F1Teal, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(med.name, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(med.dosage, color = TextGray, style = MaterialTheme.typography.bodySmall)
            }
            if (!med.isTakenToday) {
                IconButton(onClick = onMark) {
                    Icon(Icons.Default.Add, null, tint = F1Teal)
                }
            }
        }
    }
}

private fun formatMillis(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02dm %02ds", minutes, seconds)
}
