package ca.sheridancollege.medreminder.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(Modifier.height(32.dp))
        
        // Minimalist Header
        Column {
            Text(
                "PREFERENCES",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            )
            Text(
                "Settings",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(Modifier.height(32.dp))

        // Profile Section (Minimalist)
        authState.user?.let { user ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (user.photoUrl != null) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(48.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.displayName ?: "User", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(user.email ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("APP SETTINGS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        Spacer(Modifier.height(16.dp))

        MinimalSettingsRow(title = "Notifications", subtitle = "Reminders for medication") {
            Switch(
                checked = uiState.notificationsEnabled,
                onCheckedChange = viewModel::setNotificationsEnabled,
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }
        
        MinimalSettingsRow(title = "Dark Theme", subtitle = "Use dark mode aesthetics") {
            Switch(
                checked = uiState.darkTheme,
                onCheckedChange = viewModel::setDarkTheme
            )
        }

        MinimalSettingsRow(title = "Snooze Duration", subtitle = "${uiState.snoozeMinutes} minutes") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (uiState.snoozeMinutes > 5) viewModel.setSnoozeMinutes(uiState.snoozeMinutes - 5) }) {
                    Text("-", style = MaterialTheme.typography.titleLarge)
                }
                Text("${uiState.snoozeMinutes}m", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { if (uiState.snoozeMinutes < 60) viewModel.setSnoozeMinutes(uiState.snoozeMinutes + 5) }) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("DANGER ZONE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, letterSpacing = 1.sp)
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .clickable { viewModel.onResetAllRequested() }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.DeleteSweep, null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Reset All Data", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    Text("Deletes history and medications", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(48.dp))
        
        // App Info (Minimalist)
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("MEDREMINDER", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 3.sp)
            Text("v1.0.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
        
        Spacer(Modifier.height(48.dp))
    }

    // Reset Dialog (Minimalist)
    if (uiState.showResetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onResetAllDismissed() },
            title = { Text("Factory Reset", fontWeight = FontWeight.Light) },
            text = { Text("All your data will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onResetAllConfirmed() }) { Text("DELETE EVERYTHING", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onResetAllDismissed() }) { Text("CANCEL") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun MinimalSettingsRow(title: String, subtitle: String, action: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        action()
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
}
