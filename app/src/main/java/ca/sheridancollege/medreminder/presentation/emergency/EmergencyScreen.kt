package ca.sheridancollege.medreminder.presentation.emergency

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(viewModel: EmergencyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showEditContact by remember { mutableStateOf(false) }
    var showEditMedical by remember { mutableStateOf(false) }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && uiState.emergencyPhone.isNotBlank()) {
            context.startActivity(
                Intent(Intent.ACTION_CALL, Uri.parse("tel:${uiState.emergencyPhone}"))
            )
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.findNearbyHospitals(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Call 911 ───────────────────────────────────────────────
            Button(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Call, null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Call 911", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            // ── Emergency Contact ──────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Emergency Contact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showEditContact = true }) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (uiState.emergencyName.isBlank()) {
                        Text(
                            "No emergency contact set. Tap edit to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(uiState.emergencyName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        if (uiState.emergencyRelation.isNotBlank()) {
                            Text(uiState.emergencyRelation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (uiState.emergencyPhone.isNotBlank()) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { callPermissionLauncher.launch(Manifest.permission.CALL_PHONE) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Call, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Call")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.sendEmergencySms(context) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Message, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Send SMS")
                                }
                            }
                        }
                    }
                }
            }

            // ── Medical ID ─────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.LocalHospital, null, tint = MaterialTheme.colorScheme.error)
                            Text("Medical ID", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showEditMedical = true }) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    MedicalIdRow("Blood Type", uiState.bloodType.ifBlank { "Not set" })
                    MedicalIdRow("Conditions", uiState.conditions.ifBlank { "None" })
                    MedicalIdRow("Allergies", uiState.allergies.ifBlank { "None" })

                    OutlinedButton(
                        onClick = { viewModel.shareMedicationList(context) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Share Medical Info")
                    }
                }
            }

            // ── Current Medications ────────────────────────────────────
            if (uiState.medications.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Medication, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Current Medications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        uiState.medications.forEach { med ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(med.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    "${med.dosageAmount} ${med.dosageUnit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── Nearby Hospitals ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Nearby Hospitals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    if (uiState.locationError != null) {
                        Text(uiState.locationError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }

                    if (uiState.nearbyHospitals.isEmpty()) {
                        Button(
                            onClick = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isLoadingHospitals
                        ) {
                            if (uiState.isLoadingHospitals) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Searching...")
                            } else {
                                Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Find Nearby Hospitals")
                            }
                        }
                    } else {
                        uiState.nearbyHospitals.forEach { hospital ->
                            HospitalItem(hospital, context)
                        }
                        TextButton(
                            onClick = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Refresh") }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Edit Emergency Contact Dialog ──────────────────────────────────
    if (showEditContact) {
        var name by remember { mutableStateOf(uiState.emergencyName) }
        var phone by remember { mutableStateOf(uiState.emergencyPhone) }
        var relation by remember { mutableStateOf(uiState.emergencyRelation) }

        AlertDialog(
            onDismissRequest = { showEditContact = false },
            title = { Text("Emergency Contact", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Name") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), singleLine = true
                    )
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it },
                        label = { Text("Phone number") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = relation, onValueChange = { relation = it },
                        label = { Text("Relationship (e.g. Spouse)") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveEmergencyContact(name, phone, relation)
                    showEditContact = false
                }) { Text("Save") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditContact = false }) { Text("Cancel") }
            }
        )
    }

    // ── Edit Medical Info Dialog ───────────────────────────────────────
    if (showEditMedical) {
        var bloodType by remember { mutableStateOf(uiState.bloodType) }
        var conditions by remember { mutableStateOf(uiState.conditions) }
        var allergies by remember { mutableStateOf(uiState.allergies) }

        AlertDialog(
            onDismissRequest = { showEditMedical = false },
            title = { Text("Medical ID", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = bloodType, onValueChange = { bloodType = it },
                        label = { Text("Blood Type (e.g. A+)") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), singleLine = true
                    )
                    OutlinedTextField(
                        value = conditions, onValueChange = { conditions = it },
                        label = { Text("Medical Conditions") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), minLines = 2
                    )
                    OutlinedTextField(
                        value = allergies, onValueChange = { allergies = it },
                        label = { Text("Allergies") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveMedicalInfo(bloodType, conditions, allergies)
                    showEditMedical = false
                }) { Text("Save") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditMedical = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MedicalIdRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HospitalItem(hospital: NearbyHospital, context: android.content.Context) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(hospital.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(hospital.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (hospital.distance > 0) {
                    Text(HospitalFinder.formatDistance(hospital.distance), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (hospital.phone.isNotBlank()) {
                IconButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${hospital.phone}")))
                }) {
                    Icon(Icons.Default.Call, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
