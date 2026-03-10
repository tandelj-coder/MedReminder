package ca.sheridancollege.medreminder.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberTeal,
    secondary = ElectricViolet,
    background = MidnightBlue,
    surface = DeepPurple,
    onPrimary = MidnightBlue,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = GlassWhite,
    onSurfaceVariant = SoftLavender,
    outline = GlassBorder,
    error = ErrorRose
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricViolet,
    secondary = CyberTeal,
    background = LightSurface,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = MidnightBlue,
    onBackground = MidnightBlue,
    onSurface = MidnightBlue,
    surfaceVariant = Color.White,
    onSurfaceVariant = MidnightBlue,
    outline = Color.LightGray,
    error = ErrorRose
)

@Composable
fun MedReminderTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MedReminderTypography,
        content = content
    )
}
