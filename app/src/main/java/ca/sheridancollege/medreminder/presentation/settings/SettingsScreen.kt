package ca.sheridancollege.medreminder.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Section
            authState.user?.let { user ->
                ListItem(
                    headlineContent = { Text(user.displayName ?: "User", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(user.email ?: "") },
                    leadingContent = {
                        if (user.photoUrl != null) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(56.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Person, null)
                                }
                            }
                        }
                    },
                    trailingContent = {
                        IconButton(onClick = { authViewModel.signOut() }) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Preferences",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Notifications") },
                supportingContent = { Text("Receive medication reminders") },
                trailingContent = {
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Dark Theme") },
                supportingContent = { Text("Switch between light and dark mode") },
                trailingContent = {
                    Switch(
                        checked = uiState.darkTheme,
                        onCheckedChange = viewModel::setDarkTheme
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Snooze Duration") },
                supportingContent = { Text("${uiState.snoozeMinutes} minutes") },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (uiState.snoozeMinutes > 5) viewModel.setSnoozeMinutes(uiState.snoozeMinutes - 5) }) {
                            Icon(Icons.Outlined.RemoveCircleOutline, null)
                        }
                        Text("${uiState.snoozeMinutes}m", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { if (uiState.snoozeMinutes < 60) viewModel.setSnoozeMinutes(uiState.snoozeMinutes + 5) }) {
                            Icon(Icons.Outlined.AddCircleOutline, null)
                        }
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Account",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Reset All Data", color = MaterialTheme.colorScheme.error) },
                supportingContent = { Text("Deletes all medications and history") },
                trailingContent = {
                    TextButton(onClick = { viewModel.onResetAllRequested() }) {
                        Text("RESET", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            )

            Spacer(Modifier.height(32.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "MedReminder",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )
                Text(
                    "v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
            
            Spacer(Modifier.height(48.dp))
        }
    }

    if (uiState.showResetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onResetAllDismissed() },
            title = { Text("Reset All Data?") },
            text = { Text("This will permanently delete all your medications and history. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onResetAllConfirmed() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("YES, RESET")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onResetAllDismissed() }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
