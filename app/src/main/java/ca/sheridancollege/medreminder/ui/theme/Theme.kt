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
    primary = Blue40,
    onPrimary = White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Teal40,
    onSecondary = White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    tertiary = Teal50,
    onTertiary = White,
    tertiaryContainer = Teal90,
    onTertiaryContainer = Teal10,
    error = Red40,
    onError = White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = OffWhite,
    onBackground = TextDark,
    surface = LightCard,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFEEF2F7),
    onSurfaceVariant = TextMid,
    outline = Blue50,
    outlineVariant = Blue90
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal40,
    onPrimary = NavyDark,
    primaryContainer = NavyCard,
    onPrimaryContainer = Teal80,
    secondary = Teal50,
    onSecondary = NavyDark,
    secondaryContainer = NavyLight,
    onSecondaryContainer = Teal80,
    tertiary = Blue80,
    onTertiary = NavyDark,
    tertiaryContainer = NavyLight,
    onTertiaryContainer = Blue80,
    error = Red80,
    onError = Red10,
    errorContainer = Red40,
    onErrorContainer = Red90,
    background = NavyDark,
    onBackground = TextLight,
    surface = NavyCard,
    onSurface = TextLight,
    surfaceVariant = NavyMid,
    onSurfaceVariant = TextMuted,
    outline = Teal40,
    outlineVariant = NavyLight
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
