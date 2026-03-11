package ca.sheridancollege.medreminder.presentation.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.sheridancollege.medreminder.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail().build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.onSignInResult(account.idToken)
            } catch (e: ApiException) {
                Toast.makeText(context, "Sign up failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Navigate to profile setup after sign up
    LaunchedEffect(state.user) {
        if (state.user != null && state.user?.isProfileComplete == false) onSignUpSuccess()
        else if (state.user != null && state.user?.isProfileComplete == true) onSignUpSuccess()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(modifier = Modifier.size(80.dp), shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null, modifier = Modifier.padding(16.dp),
                        tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text("Create Account", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold)
                Text("Sign up to start managing your medications",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(40.dp))

            // Email
            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true, shape = MaterialTheme.shapes.large)

            Spacer(Modifier.height(14.dp))

            // Password
            OutlinedTextField(
                value = password, onValueChange = { password = it; passwordError = null },
                label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true, shape = MaterialTheme.shapes.large,
                supportingText = { Text("At least 6 characters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) }
            )

            Spacer(Modifier.height(14.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; passwordError = null },
                label = { Text("Confirm Password") }, modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true, shape = MaterialTheme.shapes.large,
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            Spacer(Modifier.height(28.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                // Sign Up button
                Button(
                    onClick = {
                        when {
                            email.isBlank() || password.isBlank() ->
                                passwordError = "Please fill in all fields"
                            password.length < 6 ->
                                passwordError = "Password must be at least 6 characters"
                            password != confirmPassword ->
                                passwordError = "Passwords do not match"
                            else -> viewModel.signUpWithEmail(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text("  OR  ", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(20.dp))

                // Google Sign Up
                OutlinedButton(
                    onClick = {
                        googleSignInClient.signOut().addOnCompleteListener {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                        Icon(painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null, modifier = Modifier.size(22.dp),
                            tint = Color.Unspecified)
                        Spacer(Modifier.width(10.dp))
                        Text("Sign up with Google", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Already have account
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?",
                    style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { viewModel.clearError(); onNavigateToLogin() }) {
                    Text("Log In", fontWeight = FontWeight.Bold)
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
