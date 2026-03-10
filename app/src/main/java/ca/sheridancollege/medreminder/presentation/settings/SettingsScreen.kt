package ca.sheridancollege.medreminder.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))

        // Streak card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "${uiState.streakCount} Day Streak",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Keep taking your medications on time!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Preferences",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        // Notifications toggle
        SettingsRow(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            subtitle = "Receive medication reminders"
        ) {
            Switch(
                checked = uiState.notificationsEnabled,
                onCheckedChange = viewModel::setNotificationsEnabled
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Dark theme toggle
        SettingsRow(
            icon = Icons.Default.DarkMode,
            title = "Dark Theme",
            subtitle = "Switch to dark mode"
        ) {
            Switch(
                checked = uiState.darkTheme,
                onCheckedChange = viewModel::setDarkTheme
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Snooze duration
        SettingsRow(
            icon = Icons.Default.Snooze,
            title = "Snooze Duration",
            subtitle = "${uiState.snoozeMinutes} minutes"
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (uiState.snoozeMinutes > 5)
                        viewModel.setSnoozeMinutes(uiState.snoozeMinutes - 5)
                }) {
                    Icon(Icons.Default.Remove, null)
                }
                Text(
                    "${uiState.snoozeMinutes}m",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = {
                    if (uiState.snoozeMinutes < 60)
                        viewModel.setSnoozeMinutes(uiState.snoozeMinutes + 5)
                }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "App Info",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("MedReminder", fontWeight = FontWeight.SemiBold)
                Text(
                    "Built with Kotlin, Jetpack Compose, Room, Hilt & WorkManager",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Sheridan College — Jigar Tandel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        action()
    }
}