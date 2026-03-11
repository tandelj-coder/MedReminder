package ca.sheridancollege.medreminder.presentation.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.sheridancollege.medreminder.R

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOut), RepeatMode.Reverse),
        label = "pulse"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Background decorative circles
        Box(modifier = Modifier.size(300.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            .align(Alignment.TopEnd).offset(x = 80.dp, y = (-60).dp))
        Box(modifier = Modifier.size(200.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f))
            .align(Alignment.BottomStart).offset(x = (-40).dp, y = 60.dp))

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(80.dp))

            // Logo + Title
            AnimatedVisibility(visible, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)) {

                    // App icon
                    Box(modifier = Modifier.size(100.dp).scale(pulse).clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center) {
                        Icon(painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).padding(4.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MedReminder", style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground)
                        Text("Your personal health companion",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center)
                    }
                }
            }

            // Feature highlights
            AnimatedVisibility(visible, enter = fadeIn(tween(800, delayMillis = 200))) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureRow(Icons.Default.Notifications, "Smart Reminders",
                        "Never miss a dose with exact-time alarms")
                    FeatureRow(Icons.Default.Favorite, "Streak Tracking",
                        "Build healthy habits with daily adherence streaks")
                    FeatureRow(Icons.Default.Shield, "Emergency Ready",
                        "SOS button, medical ID and nearby hospitals")
                }
            }

            // Buttons
            AnimatedVisibility(visible, enter = fadeIn(tween(800, delayMillis = 400)) +
                    slideInVertically(tween(600, delayMillis = 400)) { 60 }) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 48.dp)) {

                    Button(
                        onClick = onSignUpClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    OutlinedButton(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("I Already Have an Account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }

                    Text("By continuing you agree to our Terms of Service",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp))
        }
        Column {
            Text(title, fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
