package ca.sheridancollege.medreminder.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ca.sheridancollege.medreminder.presentation.auth.AuthViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header gradient same style as Today screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D2447),
                            Color(0xFF1A3A6E),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Manage your preferences",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(16.dp))

                // Test Notification button
                OutlinedButton(
                    onClick = { viewModel.sendTestNotification(context) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Notifications, null,
                        modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Test Notification", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Preferences section
        SettingsSectionHeader("Preferences")

        SettingsToggleItem(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            subtitle = "Receive medication reminders",
            checked = uiState.notificationsEnabled,
            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        SettingsToggleItem(
            icon = Icons.Default.DarkMode,
            title = "Dark Theme",
            subtitle = "Switch to dark mode",
            checked = uiState.darkTheme,
            onCheckedChange = { viewModel.setDarkTheme(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // Snooze Duration
        SettingsStepperItem(
            icon = Icons.Default.Alarm,
            title = "Snooze Duration",
            subtitle = "${uiState.snoozeMinutes} minutes",
            onDecrement = { viewModel.setSnoozeMinutes(uiState.snoozeMinutes - 5) },
            onIncrement = { viewModel.setSnoozeMinutes(uiState.snoozeMinutes + 5) },
            canDecrement = uiState.snoozeMinutes > 5,
            canIncrement = uiState.snoozeMinutes < 60
        )

        Spacer(Modifier.height(16.dp))

        // Danger zone
        SettingsSectionHeader("Danger Zone", color = MaterialTheme.colorScheme.error)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DeleteForever, null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp))
                    Column {
                        Text("Reset All Data",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error)
                        Text("Deletes all medications, history & streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                FilledTonalButton(
                    onClick = { viewModel.onResetAllRequested() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Reset") }
            }
        }

        Spacer(Modifier.height(16.dp))

        // App info
        SettingsSectionHeader("App Info")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("MedReminder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text("Built with Kotlin, Jetpack Compose, Room, Hilt & WorkManager",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Sheridan College — Jigar Tandel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Account section
        SettingsSectionHeader("Account")

        // Sign Out Button
        Button(
            onClick = {
                authViewModel.signOut()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp)) // Add some space between sign out and delete account

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.PersonOff, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Delete Account", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text("Permanently removes your account, all medications, history and data from Firebase",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                Button(
                    onClick = { viewModel.onDeleteAccountRequested() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete My Account", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }

    // Reset confirm dialog
    if (uiState.showResetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onResetAllDismissed() },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Reset All Data?") },
            text = { Text("This will delete all medications, history and reset your streak. This cannot be undone.") },
            confirmButton = {
                Button(onClick = { viewModel.onResetAllConfirmed() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Reset Everything") }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.onResetAllDismissed() }) { Text("Cancel") }
            }
        )
    }

    // Delete account confirm dialog
    if (uiState.showDeleteAccountConfirm) {
        var isDeleting by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { if (!isDeleting) viewModel.onDeleteAccountDismissed() },
            icon = { Icon(Icons.Default.PersonOff, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) },
            title = { Text("Delete Account?", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This will PERMANENTLY delete:", fontWeight = FontWeight.SemiBold)
                    Text("• Your Firebase account")
                    Text("• All medications and history")
                    Text("• All Firestore data")
                    Text("• Your streak and preferences")
                    Spacer(Modifier.height(4.dp))
                    Text("This action cannot be undone.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        authViewModel.deleteAccount(
                            onSuccess = { viewModel.onDeleteAccountDismissed() },
                            onError = { isDeleting = false }
                        )
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Yes, Delete Forever", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.onDeleteAccountDismissed() }, enabled = !isDeleting) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingsStepperItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    canDecrement: Boolean,
    canIncrement: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, enabled = canDecrement) {
                Icon(Icons.Default.Remove, null)
            }
            IconButton(onClick = onIncrement, enabled = canIncrement) {
                Icon(Icons.Default.Add, null)
            }
        }
    }
}
