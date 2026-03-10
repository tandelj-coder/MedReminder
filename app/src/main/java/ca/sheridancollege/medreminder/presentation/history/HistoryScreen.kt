package ca.sheridancollege.medreminder.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.domain.model.IntakeLog

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.logs.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("No history yet", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Your intake log will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.logs) { log ->
                    IntakeLogCard(log)
                }
            }
        }
    }
}

@Composable
fun IntakeLogCard(log: IntakeLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (log.wasDoubleDoseAttempt)
                MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (log.wasDoubleDoseAttempt)
                    Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (log.wasDoubleDoseAttempt) MaterialTheme.colorScheme.error
                else Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(log.medicationName, fontWeight = FontWeight.SemiBold)
                Text(
                    log.formattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (log.snoozeReason.isNotBlank()) {
                    Text(
                        "Reason: ${log.snoozeReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    log.formattedTime(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (log.wasOnTime) "On time" else "Late",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (log.wasOnTime) Color(0xFF4CAF50)
                    else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}