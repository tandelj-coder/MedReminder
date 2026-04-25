package ca.sheridancollege.medreminder.presentation.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.DoseEvent
import ca.sheridancollege.medreminder.domain.model.DoseStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val progress = if (uiState.adherenceStats.totalScheduled > 0) 
        uiState.adherenceStats.totalTaken.toFloat() / uiState.adherenceStats.totalScheduled 
    else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Today", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Daily Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                        )
                        Text("${uiState.adherenceStats.totalTaken} of ${uiState.adherenceStats.totalScheduled} doses taken", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (uiState.lowStockMedications.isNotEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(Modifier.width(8.dp))
                            Text("Low stock: ${uiState.lowStockMedications.joinToString { it.name }}", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            uiState.groupedDoses.forEach { group ->
                item { Text(group.timeLabel, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                items(group.doses, key = { it.id }) { dose ->
                    DoseCard(
                        dose = dose,
                        onTake = { viewModel.onMarkAsTaken(dose) },
                        onSkip = { viewModel.onSkipDose(dose) }
                    )
                }
            }
        }
    }
}

@Composable
fun DoseCard(dose: DoseEvent, onTake: () -> Unit, onSkip: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(dose.medicationName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(dose.scheduledTime)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (dose.status == DoseStatus.TAKEN) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32))
                }
            }

            if (dose.status == DoseStatus.SCHEDULED) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onTake, modifier = Modifier.weight(1f)) { Text("Take") }
                    OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) { Text("Skip") }
                }
            }
        }
    }
}
