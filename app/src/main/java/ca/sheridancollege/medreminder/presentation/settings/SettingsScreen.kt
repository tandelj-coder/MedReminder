package ca.sheridancollege.medreminder.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.presentation.auth.AuthViewModel
import coil.compose.AsyncImage

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.state.collectAsState()

    // Reset done snackbar
    if (uiState.resetDone) {
        LaunchedEffect(Unit) {
            viewModel.onResetDoneDismissed()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        // Profile Section
        authState.user?.let { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (user.photoUrl != null) {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(12.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            user.displayName ?: "User",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            user.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // Streak card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (uiState.streakCount == 0) "😴" else if (uiState.streakCount < 3) "🌱" else if (uiState.streakCount < 7) "🔥" else "🏆",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${uiState.streakCount} Day Streak",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        when {
                            uiState.streakCount == 0 -> "Start your streak today!"
                            uiState.streakCount < 3 -> "Great start, keep going!"
                            uiState.streakCount < 7 -> "You're on fire! 🔥"
                            else -> "Incredible discipline! 🏆"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                // Streak dots (last 7 days visual)
                Column(horizontalAlignment = Alignment.End) {
                    Text("Last 7 days", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(7) { i ->
                            val filled = i < minOf(uiState.streakCount, 7)
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = if (filled) Color(0xFFFF6B35)
                                        else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                modifier = Modifier.size(10.dp)
                            ) {}
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Preferences", style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))

        SettingsRow(icon = Icons.Default.Notifications, title = "Notifications",
            subtitle = "Receive medication reminders") {
            Switch(checked = uiState.notificationsEnabled, onCheckedChange = viewModel::setNotificationsEnabled)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        SettingsRow(icon = Icons.Default.DarkMode, title = "Dark Theme",
            subtitle = "Switch to dark mode") {
            Switch(checked = uiState.darkTheme, onCheckedChange = viewModel::setDarkTheme)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        SettingsRow(icon = Icons.Default.Snooze, title = "Snooze Duration",
            subtitle = "${uiState.snoozeMinutes} minutes") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (uiState.snoozeMinutes > 5) viewModel.setSnoozeMinutes(uiState.snoozeMinutes - 5) }) {
                    Icon(Icons.Default.Remove, null)
                }
                Text("${uiState.snoozeMinutes}m", style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { if (uiState.snoozeMinutes < 60) viewModel.setSnoozeMinutes(uiState.snoozeMinutes + 5) }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Danger Zone", style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.DeleteForever, null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
                    Column {
                        Text("Reset All Data", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("Deletes all medications,\nhistory & streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                    }
                }
                Button(
                    onClick = { viewModel.onResetAllRequested() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Reset", fontWeight = FontWeight.Bold) }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("App Info", style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("MedReminder", fontWeight = FontWeight.SemiBold)
                Text("Built with Kotlin, Jetpack Compose, Room, Hilt & WorkManager",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Sheridan College — Jigar Tandel",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(32.dp))
    }

    // Reset confirmation dialog
    if (uiState.showResetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onResetAllDismissed() },
            icon = {
                Icon(Icons.Default.DeleteForever, null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp))
            },
            title = { Text("Reset All Data?", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This will permanently delete:", fontWeight = FontWeight.SemiBold)
                    Text("• All medications")
                    Text("• All intake history")
                    Text("• Your streak count")
                    Spacer(Modifier.height(4.dp))
                    Text("This cannot be undone.", color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.onResetAllConfirmed() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Yes, Delete Everything") }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.onResetAllDismissed() }) { Text("Cancel") }
            }
        )
    }

    // Reset done dialog
    if (uiState.resetDone) {
        AlertDialog(
            onDismissRequest = { viewModel.onResetDoneDismissed() },
            icon = { Text("✅", fontSize = 36.sp) },
            title = { Text("All Data Cleared") },
            text = { Text("Your app has been reset to factory settings.") },
            confirmButton = {
                Button(onClick = { viewModel.onResetDoneDismissed() }) { Text("OK") }
            }
        )
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        action()
    }
}
