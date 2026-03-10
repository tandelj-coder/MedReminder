package ca.sheridancollege.medreminder.presentation.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.Medication
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onEditMedication: (Int) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "MedReminder",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Profile */ }) {
                        Icon(Icons.Outlined.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = { viewModel.onResetTodayRequested() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* This usually navigates to Add, but we use Bottom Nav */ },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Medication") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Daily Progress",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "${uiState.adherenceStats.totalTaken}/${uiState.adherenceStats.totalScheduled} doses taken",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            "🔥 ${uiState.adherenceStats.currentStreak}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            // Next Dose
            uiState.nextDoseInfo?.let {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = { Text("Next Dose: ${it.medicationName}") },
                            supportingContent = { Text("Scheduled Today") },
                            trailingContent = {
                                Text(
                                    formatMillis(it.remainingTimeMillis),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
            }

            val pending = uiState.medications.filter { !it.isTakenToday }
            val taken = uiState.medications.filter { it.isTakenToday }

            if (pending.isNotEmpty()) {
                item { 
                    Text(
                        "Pending", 
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    ) 
                }
                items(pending) { med ->
                    MedicationListItem(
                        medication = med,
                        onMark = { viewModel.onMarkAsTaken(med) },
                        onEdit = { onEditMedication(med.id) }
                    )
                }
            }

            if (taken.isNotEmpty()) {
                item { 
                    Text(
                        "Completed", 
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    ) 
                }
                items(taken) { med ->
                    MedicationListItem(
                        medication = med,
                        onMark = {},
                        onEdit = { onEditMedication(med.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationListItem(
    medication: Medication,
    onMark: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        ListItem(
            headlineContent = { Text(medication.name) },
            supportingContent = { Text("${medication.dosage} • ${medication.formattedTime()}") },
            leadingContent = {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (medication.isTakenToday) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            medication.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            trailingContent = {
                if (!medication.isTakenToday) {
                    FilledIconButton(onClick = onMark) {
                        Icon(Icons.Default.Check, contentDescription = "Mark Taken")
                    }
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Taken",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

private fun formatMillis(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format(Locale.getDefault(), "%02dm %02ds", minutes, seconds)
}
