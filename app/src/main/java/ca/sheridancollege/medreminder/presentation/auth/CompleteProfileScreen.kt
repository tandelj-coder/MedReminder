package ca.sheridancollege.medreminder.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    viewModel: AuthViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    var name by remember { mutableStateOf(state.user?.displayName ?: "") }
    var phone by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var illness by remember { mutableStateOf("") }

    LaunchedEffect(state.user?.isProfileComplete) {
        if (state.user?.isProfileComplete == true) {
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("COMPLETE PROFILE", fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "We need a few more details to personalize your experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                leadingIcon = { Icon(Icons.Outlined.Person, null) }
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                leadingIcon = { Icon(Icons.Outlined.Phone, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height (cm)") },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            OutlinedTextField(
                value = illness,
                onValueChange = { illness = it },
                label = { Text("Current Illness/Condition") },
                placeholder = { Text("e.g. Diabetes, Hypertension") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                minLines = 3
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.updateProfile(name, phone, height, weight, illness)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("FINISH SETUP", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}
