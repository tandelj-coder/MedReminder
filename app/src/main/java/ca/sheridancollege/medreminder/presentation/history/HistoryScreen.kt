package ca.sheridancollege.medreminder.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Warning
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
import ca.sheridancollege.medreminder.domain.model.IntakeLog
import ca.sheridancollege.medreminder.ui.theme.*
import java.util.*

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DeepPaddock
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(32.dp))
            
            Text(
                "LOGBOOK",
                style = MaterialTheme.typography.labelMedium,
                color = TextGray,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Session History",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            Spacer(Modifier.height(32.dp))

            when {
                uiState.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = RacingRed, strokeWidth = 3.dp) }

                uiState.logs.isEmpty() -> EmptyHistoryState()

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(uiState.logs) { log ->
                            PaddockHistoryCard(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaddockHistoryCard(log: IntakeLog) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardDark,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (log.wasDoubleDoseAttempt) RacingRed.copy(alpha = 0.5f) else RacingTeal.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High-Contrast Status Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (log.wasDoubleDoseAttempt) RacingRed.copy(alpha = 0.1f)
                        else RacingTeal.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (log.wasDoubleDoseAttempt) Icons.Outlined.Warning 
                                 else Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = if (log.wasDoubleDoseAttempt) RacingRed else RacingTeal,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    log.medicationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    log.formattedDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    log.formattedTime(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    if (log.wasOnTime) "ON TIME" else "LATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (log.wasOnTime) RacingTeal else RacingRed,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.History, null, tint = TextGray, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                "No telemetry data.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
