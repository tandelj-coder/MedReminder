package ca.sheridancollege.medreminder.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    secondary = AccentSecondary,
    background = DeepBlack,
    surface = DarkGray,
    onPrimary = Color.White,
    onSecondary = DarkTextPrimary,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBorder,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = AccentPrimary,
    secondary = AccentSecondary,
    background = SoftGray,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = BorderGray,
    onSurfaceVariant = TextSecondary,
    outline = BorderGray,
    error = ErrorRed
)

@Composable
fun MedReminderTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // We'll keep this off for a consistent minimalist look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MedReminderTypography,
        content = content
    )
}
