package ca.sheridancollege.medreminder.presentation.emergency

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EmergencyScreen(viewModel: EmergencyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSosConfirm by remember { mutableStateOf(false) }
    var showEditContact by remember { mutableStateOf(false) }
    var showEditMedical by remember { mutableStateOf(false) }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:911"))
            context.startActivity(intent)
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && uiState.emergencyPhone.isNotBlank()) {
            viewModel.sendEmergencySms(context)
        }
    }

    // Auto-fetch nearest hospitals when screen opens
    LaunchedEffect(Unit) {
        if (uiState.nearbyHospitals.isEmpty()) {
            viewModel.findNearbyHospitals(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF8B0000), Color(0xFFB71C1C), MaterialTheme.colorScheme.background)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text("Emergency", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Text("Quick access to emergency services",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f))
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── SOS Button ──────────────────────────────────────────
            SosButton(onClick = { showSosConfirm = true })

            // ── Quick Actions ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Phone,
                    label = "Call 911",
                    color = Color(0xFFD32F2F),
                    onClick = { callPermissionLauncher.launch(Manifest.permission.CALL_PHONE) }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = if (uiState.isLoadingHospitals) Icons.Default.HourglassEmpty
                           else Icons.Default.LocalHospital,
                    label = uiState.nearbyHospitals.firstOrNull()
                        ?.name?.split(" ")?.take(2)?.joinToString(" ")
                        ?: if (uiState.isLoadingHospitals) "Finding..." else "Hospital",
                    color = Color(0xFF1565C0),
                    onClick = {
                        val number = uiState.nearbyHospitals.firstOrNull()
                            ?.phone?.ifBlank { null } ?: "911"
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
                    }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.Message,
                    label = "Alert Contact",
                    color = Color(0xFF2E7D32),
                    onClick = { smsPermissionLauncher.launch(Manifest.permission.SEND_SMS) }
                )
            }

            // ── Emergency Contact Card ────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.ContactPhone, null,
                                tint = MaterialTheme.colorScheme.primary)
                            Text("Emergency Contact", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showEditContact = true }) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp))
                        }
                    }

                    if (uiState.emergencyName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center) {
                                Text(uiState.emergencyName.first().uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(uiState.emergencyName, fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge)
                                Text(uiState.emergencyRelation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(uiState.emergencyPhone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL,
                                    Uri.parse("tel:${uiState.emergencyPhone}"))
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Default.Phone, null,
                                    tint = Color(0xFF2E7D32))
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showEditContact = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Emergency Contact")
                        }
                    }
                }
            }

            // ── Medical ID Card ──────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.MedicalInformation, null,
                                tint = Color(0xFF1565C0))
                            Text("Medical ID", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showEditMedical = true }) {
                            Icon(Icons.Default.Edit, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp))
                        }
                    }

                    MedicalInfoRow("🩸 Blood Type", uiState.bloodType.ifBlank { "Not set" })
                    MedicalInfoRow("🏥 Conditions", uiState.conditions.ifBlank { "None" })
                    MedicalInfoRow("⚠️ Allergies", uiState.allergies.ifBlank { "None" })

                    HorizontalDivider()

                    // Current medications
                    Text("💊 Current Medications",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (uiState.medications.isEmpty()) {
                        Text("No medications added",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        uiState.medications.forEach { med ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary))
                                Text("${med.name} · ${med.dosage} · ${med.formattedTime()}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Share button
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

            // ── Disclaimer ──────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Info, null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Text("In a life-threatening emergency, always call 911 directly. This app is not a substitute for emergency services.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    // SOS Confirm Dialog
    if (showSosConfirm) {
        AlertDialog(
            onDismissRequest = { showSosConfirm = false },
            icon = { Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(36.dp)) },
            title = { Text("Send SOS?", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("This will:")
                    Text("• Call 911 immediately")
                    if (uiState.emergencyPhone.isNotBlank())
                        Text("• Send SMS to ${uiState.emergencyName} with your medication list")
                    Text("• Share your medical information")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSosConfirm = false
                        callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                        if (uiState.emergencyPhone.isNotBlank())
                            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("SEND SOS", fontWeight = FontWeight.Black) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSosConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Contact Dialog
    if (showEditContact) {
        EditContactDialog(
            currentName = uiState.emergencyName,
            currentPhone = uiState.emergencyPhone,
            currentRelation = uiState.emergencyRelation,
            onSave = { name, phone, relation ->
                viewModel.saveEmergencyContact(name, phone, relation)
                showEditContact = false
            },
            onDismiss = { showEditContact = false }
        )
    }

    // Edit Medical Info Dialog
    if (showEditMedical) {
        EditMedicalDialog(
            currentBloodType = uiState.bloodType,
            currentConditions = uiState.conditions,
            currentAllergies = uiState.allergies,
            onSave = { blood, conditions, allergies ->
                viewModel.saveMedicalInfo(blood, conditions, allergies)
                showEditMedical = false
            },
            onDismiss = { showEditMedical = false }
        )
    }
}

@Composable
fun SosButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "sosScale"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing ring
        Box(
            modifier = Modifier
                .size((140 * scale).dp)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = 0.15f))
        )
        Button(
            onClick = onClick,
            modifier = Modifier.size(120.dp).scale(scale),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Emergency, null, modifier = Modifier.size(32.dp),
                    tint = Color.White)
                Text("SOS", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = color, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MedicalInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EditContactDialog(
    currentName: String, currentPhone: String, currentRelation: String,
    onSave: (String, String, String) -> Unit, onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var phone by remember { mutableStateOf(currentPhone) }
    var relation by remember { mutableStateOf(currentRelation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Emergency Contact", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Person, null) })
                OutlinedTextField(value = phone, onValueChange = { phone = it },
                    label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Phone, null) })
                OutlinedTextField(value = relation, onValueChange = { relation = it },
                    label = { Text("Relationship (e.g. Mom, Doctor)") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Favorite, null) })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, phone, relation) }) { Text("Save") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditMedicalDialog(
    currentBloodType: String, currentConditions: String, currentAllergies: String,
    onSave: (String, String, String) -> Unit, onDismiss: () -> Unit
) {
    var bloodType by remember { mutableStateOf(currentBloodType) }
    var conditions by remember { mutableStateOf(currentConditions) }
    var allergies by remember { mutableStateOf(currentAllergies) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Medical Information", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = bloodType, onValueChange = { bloodType = it },
                    label = { Text("Blood Type (e.g. A+, O-)") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Text("🩸", modifier = Modifier.padding(start = 4.dp)) })
                OutlinedTextField(value = conditions, onValueChange = { conditions = it },
                    label = { Text("Medical Conditions") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), minLines = 2,
                    leadingIcon = { Icon(Icons.Default.MedicalServices, null) })
                OutlinedTextField(value = allergies, onValueChange = { allergies = it },
                    label = { Text("Allergies") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Warning, null) })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(bloodType, conditions, allergies) }) { Text("Save") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun NearbyHospitalsCard(
    hospitals: List<NearbyHospital>,
    isLoading: Boolean,
    locationError: String?,
    onFindHospitals: () -> Unit,
    onCallHospital: (String) -> Unit
) {
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) onFindHospitals() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocalHospital, null, tint = Color(0xFF1565C0))
                    Text("Nearby Hospitals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
                if (!isLoading) {
                    IconButton(onClick = {
                        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }) {
                        Icon(Icons.Default.MyLocation, null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            when {
                isLoading -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Finding nearest hospitals...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                locationError != null -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp))
                        Text(locationError, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
                hospitals.isEmpty() -> {
                    OutlinedButton(
                        onClick = {
                            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Find Nearest Hospitals")
                    }
                }
                else -> {
                    hospitals.forEach { hospital ->
                        HospitalItem(hospital = hospital, onCall = { onCallHospital(hospital.phone) })
                        if (hospital != hospitals.last()) HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalItem(hospital: NearbyHospital, onCall: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1565C0).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.LocalHospital, null,
                tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(hospital.name, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
            Text(hospital.address, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (hospital.distance > 0) {
                Text(HospitalFinder.formatDistance(hospital.distance),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
        if (hospital.phone.isNotBlank()) {
            FilledTonalIconButton(onClick = onCall,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF2E7D32).copy(alpha = 0.15f)
                )) {
                Icon(Icons.Default.Phone, null,
                    tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
            }
        }
    }
}
