package ca.sheridancollege.medreminder.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E3FF),
    onPrimaryContainer = Color(0xFF001849),
    secondary = Color(0xFF00BCD4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF003A42),
    tertiary = Color(0xFF1976D2),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE3F2FD),
    onTertiaryContainer = Color(0xFF0D1B2A),
    error = Color(0xFFE53935),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFF3B0007),
    background = Color(0xFFF5F7FA),      // light grey-white
    onBackground = Color(0xFF0A1628),
    surface = Color(0xFFFFFFFF),          // pure white cards
    onSurface = Color(0xFF0A1628),
    surfaceVariant = Color(0xFFEEF2F7),
    onSurfaceVariant = Color(0xFF4A5568),
    outline = Color(0xFF1565C0),
    outlineVariant = Color(0xFFD4E3FF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00BCD4),
    onPrimary = Color(0xFF0A1628),
    primaryContainer = Color(0xFF152B55),
    onPrimaryContainer = Color(0xFF80DEEA),
    secondary = Color(0xFF26C6DA),
    onSecondary = Color(0xFF0A1628),
    secondaryContainer = Color(0xFF1A3A6E),
    onSecondaryContainer = Color(0xFF80DEEA),
    tertiary = Color(0xFF90CAF9),
    onTertiary = Color(0xFF0A1628),
    tertiaryContainer = Color(0xFF1A3A6E),
    onTertiaryContainer = Color(0xFF90CAF9),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF3B0007),
    errorContainer = Color(0xFFE53935),
    onErrorContainer = Color(0xFFFFEBEE),
    background = Color(0xFF0A1628),       // deep navy
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF152B55),          // navy card
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF0D2447),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF00BCD4),
    outlineVariant = Color(0xFF1A3A6E)
)

@Composable
fun MedReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MedReminderTypography,
        content = content
    )
}
