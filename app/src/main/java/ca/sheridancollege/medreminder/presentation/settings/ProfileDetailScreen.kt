package ca.sheridancollege.medreminder.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun ProfileDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val user = state.user

    var isEditing by remember { mutableStateOf(false) }
    
    // Editable fields
    var name by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var phone by remember(user) { mutableStateOf(user?.phoneNumber ?: "") }
    var height by remember(user) { mutableStateOf(user?.height ?: "") }
    var weight by remember(user) { mutableStateOf(user?.weight ?: "") }
    var illness by remember(user) { mutableStateOf(user?.illness ?: "") }
    var photoUrl by remember(user) { mutableStateOf(user?.photoUrl) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // In a real app, you'd upload to Firebase Storage and get a download URL.
            // For now, we'll store the local string URI to demonstrate the UI update.
            photoUrl = it.toString()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MY PROFILE", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        if (isEditing) {
                            viewModel.updateProfile(name, phone, height, weight, illness, photoUrl)
                            isEditing = false
                        } else {
                            isEditing = true
                        }
                    }) {
                        Text(if (isEditing) "SAVE" else "EDIT", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Profile Picture
            Box(contentAlignment = Alignment.BottomEnd) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Person, null, modifier = Modifier.size(60.dp))
                        }
                    }
                }
                
                if (isEditing) {
                    SmallFloatingActionButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Personal Info Blocks
            ProfileInfoField(label = "NAME", value = name, isEditing = isEditing, onValueChange = { name = it }, icon = Icons.Outlined.Badge)
            ProfileInfoField(label = "PHONE", value = phone, isEditing = isEditing, onValueChange = { phone = it }, icon = Icons.Outlined.Phone)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProfileInfoField(label = "HEIGHT (CM)", value = height, isEditing = isEditing, onValueChange = { height = it }, modifier = Modifier.weight(1f))
                ProfileInfoField(label = "WEIGHT (KG)", value = weight, isEditing = isEditing, onValueChange = { weight = it }, modifier = Modifier.weight(1f))
            }

            ProfileInfoField(label = "MEDICAL CONDITION", value = illness, isEditing = isEditing, onValueChange = { illness = it }, icon = Icons.Outlined.MedicalInformation, singleLine = false)

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileInfoField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                leadingIcon = icon?.let { { Icon(it, null) } },
                singleLine = singleLine
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(16.dp))
                    }
                    Text(value.ifBlank { "Not set" }, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
