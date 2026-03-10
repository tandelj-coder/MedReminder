package ca.sheridancollege.medreminder.presentation.today

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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

@Composable
fun TodayScreen(
    onEditMedication: (Int) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(SurfaceGradient))
    ) {
        // Aesthetic Blurred Background Elements
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-50).dp)
                .blur(100.dp)
                .clip(CircleShape)
                .background(ElectricViolet.copy(alpha = 0.2f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(40.dp))

                // Aesthetic Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()),
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberTeal,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "My Meds",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    
                    Surface(
                        onClick = { viewModel.onResetTodayRequested() },
                        shape = CircleShape,
                        color = GlassWhite,
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Icon(
                            Icons.Outlined.RestartAlt,
                            contentDescription = "Reset",
                            modifier = Modifier.padding(12.dp).size(20.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Glassmorphism Stats Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = GlassWhite,
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatColumn("Taken", "${uiState.adherenceStats.totalTaken}/${uiState.adherenceStats.totalScheduled}")
                        VerticalDivider(modifier = Modifier.height(30.dp), thickness = 1.dp, color = GlassBorder)
                        StatColumn("Streak", "🔥 ${uiState.adherenceStats.currentStreak}")
                        VerticalDivider(modifier = Modifier.height(30.dp), thickness = 1.dp, color = GlassBorder)
                        StatColumn("Goal", "100%")
                    }
                }

                uiState.nextDoseInfo?.let {
                    Spacer(Modifier.height(24.dp))
                    AestheticTimer(it)
                }

                Spacer(Modifier.height(32.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    val pending = uiState.medications.filter { !it.isTakenToday }
                    val taken = uiState.medications.filter { it.isTakenToday }

                    if (pending.isNotEmpty()) {
                        item { Text("UPCOMING", style = MaterialTheme.typography.labelLarge, color = SoftLavender) }
                        items(pending) { med ->
                            AestheticMedCard(med, onMark = { viewModel.onMarkAsTaken(med) }, onEdit = { onEditMedication(med.id) })
                        }
                    }

                    if (taken.isNotEmpty()) {
                        item { Spacer(Modifier.height(8.dp)); Text("COMPLETED", style = MaterialTheme.typography.labelLarge, color = CyberTeal) }
                        items(taken) { med ->
                            AestheticMedCard(med, onMark = {}, onEdit = { onEditMedication(med.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = SoftLavender)
    }
}

@Composable
fun AestheticTimer(info: NextDoseInfo) {
    val totalSeconds = (info.remainingTimeMillis / 1000).coerceAtLeast(0)
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, CyberTeal.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberTeal))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("NEXT DOSE", style = MaterialTheme.typography.labelSmall, color = CyberTeal)
                    Text(info.medicationName, style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
            Text(
                String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
fun AestheticMedCard(med: Medication, onMark: () -> Unit, onEdit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        color = if (med.isTakenToday) GlassWhite.copy(alpha = 0.05f) else GlassWhite,
        border = BorderStroke(1.dp, if (med.isTakenToday) Color.Transparent else GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(med.formattedTime().split(" ")[0], color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(med.name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(med.dosage, style = MaterialTheme.typography.labelMedium, color = SoftLavender)
            }

            if (!med.isTakenToday) {
                IconButton(onClick = onMark) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = CyberTeal)
                }
            } else {
                Icon(Icons.Outlined.TaskAlt, null, tint = CyberTeal.copy(alpha = 0.5f))
            }
        }
    }
}
